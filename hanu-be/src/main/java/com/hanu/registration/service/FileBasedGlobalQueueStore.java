package com.hanu.registration.service;

import com.hanu.registration.model.Course;
import com.hanu.registration.model.GlobalQueueEntry;
import com.hanu.registration.model.RegistrationRecord;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileBasedGlobalQueueStore implements GlobalQueueStore {

    private static final String DATA_DIR = "runtime-data";
    private static final String DATA_FILE = "runtime-data/global-queue.dat";

    private final Object lock = new Object();

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Path filePath = Paths.get(DATA_FILE);
            if (!Files.exists(filePath)) {
                saveEntries(new ArrayList<>());
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize global queue data file", e);
        }
    }

    @Override
    public GlobalQueueEntry registerAdd(String studentId, RegistrationRecord record) {
        synchronized (lock) {
            List<GlobalQueueEntry> entries = safeLoadEntries();

            Long courseId = record.getCourse() != null ? record.getCourse().getId() : null;
            if (courseId == null) {
                throw new IllegalArgumentException("Course ID is null");
            }

            boolean alreadyActive = entries.stream().anyMatch(e ->
                    e != null
                            && e.isActive()
                            && safeEquals(e.getStudentId(), studentId)
                            && safeEquals(e.getCourseId(), courseId));

            if (alreadyActive) {
                return entries.stream()
                        .filter(e -> e != null
                                && e.isActive()
                                && safeEquals(e.getStudentId(), studentId)
                                && safeEquals(e.getCourseId(), courseId))
                        .findFirst()
                        .orElse(null);
            }

            long nextSequence = entries.stream()
                    .filter(e -> e != null && e.getGlobalSequence() != null)
                    .map(GlobalQueueEntry::getGlobalSequence)
                    .max(Long::compareTo)
                    .orElse(0L) + 1L;

            Course course = record.getCourse();

            GlobalQueueEntry entry = new GlobalQueueEntry();
            entry.setGlobalSequence(nextSequence);
            entry.setStudentId(studentId);
            entry.setCourseId(courseId);
            entry.setCourseCode(course != null ? course.getCourseCode() : "");
            entry.setCourseName(course != null
                    ? (course.getCourseNameEn() != null && !course.getCourseNameEn().isBlank()
                    ? course.getCourseNameEn()
                    : course.getGroupName())
                    : "");
            entry.setIdToHoc(course != null ? course.getIdToHoc() : null);
            entry.setLocalPriority(record.getPriority());
            entry.setCredits(course != null ? course.getCredits() : 0);

            int availableSlots = 0;
            if (course != null) {
                availableSlots = Math.max(0, course.getCapacity() - course.getEnrolled());
            }
            entry.setAvailableSlots(availableSlots);
            entry.setScheduleTime(course != null ? course.getScheduleTime() : "");
            entry.setCreatedAt(LocalDateTime.now());
            entry.setActive(true);

            entries.add(entry);
            entries.sort(Comparator.comparing(
                    GlobalQueueEntry::getGlobalSequence,
                    Comparator.nullsLast(Long::compareTo)
            ));
            saveEntries(entries);

            return entry;
        }
    }

    @Override
    public void markRemoved(String studentId, Long courseId) {
        synchronized (lock) {
            List<GlobalQueueEntry> entries = safeLoadEntries();

            for (GlobalQueueEntry entry : entries) {
                if (entry != null
                        && entry.isActive()
                        && safeEquals(entry.getStudentId(), studentId)
                        && safeEquals(entry.getCourseId(), courseId)) {
                    entry.setActive(false);
                }
            }

            saveEntries(entries);
        }
    }

    @Override
    public void markAllRemovedForStudent(String studentId) {
        synchronized (lock) {
            List<GlobalQueueEntry> entries = safeLoadEntries();

            for (GlobalQueueEntry entry : entries) {
                if (entry != null
                        && entry.isActive()
                        && safeEquals(entry.getStudentId(), studentId)) {
                    entry.setActive(false);
                }
            }
            saveEntries(entries);
        }
    }

    @Override
    public List<GlobalQueueEntry> getAllEntries() {
        synchronized (lock) {
            return new ArrayList<>(safeLoadEntries());
        }
    }

    @Override
    public List<GlobalQueueEntry> getActiveEntries() {
        synchronized (lock) {
            return safeLoadEntries().stream()
                    .filter(e -> e != null && e.isActive())
                    .sorted(Comparator.comparing(
                            GlobalQueueEntry::getGlobalSequence,
                            Comparator.nullsLast(Long::compareTo)
                    ))
                    .collect(Collectors.toList());
        }
    }

    public void overwriteAllEntries(List<GlobalQueueEntry> entries) {
        synchronized (lock) {
            saveEntries(entries);
        }
    }

    @SuppressWarnings("unchecked")
    private List<GlobalQueueEntry> loadEntries() {
        Path filePath = Paths.get(DATA_FILE);
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                return (List<GlobalQueueEntry>) obj;
            }
            return new ArrayList<>();
        } catch (EOFException e) {
            return new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("Cannot read global queue file", e);
        }
    }

    private List<GlobalQueueEntry> safeLoadEntries() {
        try {
            return loadEntries();
        } catch (Exception e) {
            System.out.println("GLOBAL QUEUE LOAD ERROR = " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveEntries(List<GlobalQueueEntry> entries) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(entries);
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException("Cannot write global queue file", e);
        }
    }

    private boolean safeEquals(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}