package com.ai;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ExamScheduler {

    // Класс для представления экзамена (по предмету для конкретной секции)
    static class Exam {
        String subject;
        String course;
        String section; // студенты из одной секции сдают экзамен в один день
        Set<Integer> studentIds; // уникальные идентификаторы студентов
        String instructor;  // рекомендованный проктор

        public Exam(String subject, String course, String section, String instructor) {
            this.subject = subject;
            this.course = course;
            this.section = section;
            this.instructor = instructor;
            this.studentIds = new HashSet<>();
        }

        @Override
        public String toString() {
            return subject + " (" + course + ") - секция " + section;
        }
    }

    // Класс для представления аудитории
    static class Auditorium {
        String roomId;   // идентификатор аудитории как строка
        String roomType;
        int capacity;

        public Auditorium(String roomId, String roomType, int capacity) {
            this.roomId = roomId;
            this.roomType = roomType;
            this.capacity = capacity;
        }

        @Override
        public String toString() {
            return "Аудитория " + roomId + " (" + roomType + ", вместимость: " + capacity + ")";
        }
    }

    // Чтение данных экзаменов из Excel-файла
    public static List<Map<String, String>> readExamData(String filename) throws IOException {
        System.out.println("Чтение данных экзаменов из файла: " + filename);
        List<Map<String, String>> rows = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filename);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue().trim());
            }
            System.out.println("Заголовки: " + headers);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Map<String, String> data = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String value;
                    switch (cell.getCellType()) {
                        case NUMERIC:
                            value = String.valueOf((int) cell.getNumericCellValue());
                            break;
                        case STRING:
                            value = cell.getStringCellValue().trim();
                            break;
                        default:
                            value = "";
                    }
                    data.put(headers.get(j), value);
                }
                rows.add(data);
            }
        }
        System.out.println("Прочитано " + rows.size() + " строк данных экзаменов.");
        return rows;
    }

    // Чтение данных аудиторий из Excel-файла
    public static List<Auditorium> readAuditoriums(String filename) throws IOException {
        System.out.println("Чтение данных аудиторий из файла: " + filename);
        List<Auditorium> auditoriums = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filename);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            // Первая строка - заголовок, данные со второй строки
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                // Читаем roomId как строку:
                String roomId;
                Cell cell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                if (cell.getCellType() == CellType.NUMERIC) {
                    roomId = String.valueOf((int) cell.getNumericCellValue());
                } else {
                    roomId = cell.getStringCellValue().trim();
                }
                String roomType = row.getCell(1).getStringCellValue().trim();
                int capacity = getNumericCellValue(row.getCell(2));
                auditoriums.add(new Auditorium(roomId, roomType, capacity));
            }
        }
        System.out.println("Прочитано " + auditoriums.size() + " аудиторий.");
        return auditoriums;
    }

    // Вспомогательный метод для получения числового значения из ячейки
    private static int getNumericCellValue(Cell cell) {
        if (cell == null) return 0;
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Не удалось распарсить числовое значение: " + cell.getStringCellValue());
                    return 0;
                }
            default:
                return 0;
        }
    }

    public static void main(String[] args) throws IOException {
        Loader.loadNativeLibraries();
        System.out.println("Запуск алгоритма распределения экзаменов...");

        // Задайте корректные пути к файлам
        String examDataFile = "C:\\Users\\himka\\IdeaProjects\\Practice1\\src\\main\\java\\com\\ai\\FakedNarxozData.xlsx";
        String auditoriumsFile = "C:\\Users\\himka\\IdeaProjects\\Practice1\\src\\main\\java\\com\\ai\\auditoriums.xlsx";

        // Чтение данных
        List<Map<String, String>> dataRows = readExamData(examDataFile);
        List<Auditorium> auditoriumList = readAuditoriums(auditoriumsFile);

        // Группировка экзаменов по уникальному сочетанию Subject + Section
        Map<String, Exam> examMap = new HashMap<>();
        for (Map<String, String> row : dataRows) {
            String subject = row.get("Subject");
            String course = row.get("Course");
            String section = row.get("Section");
            String instructor = row.get("Instructor");
            String fakeId = row.get("fake_id");
            String key = subject + "_" + section;
            Exam exam = examMap.get(key);
            if (exam == null) {
                exam = new Exam(subject, course, section, instructor);
                examMap.put(key, exam);
            }
            try {
                exam.studentIds.add(Integer.parseInt(fakeId));
            } catch (NumberFormatException e) {
                System.out.println("Ошибка при разборе fake_id: " + fakeId);
            }
        }
        List<Exam> exams = new ArrayList<>(examMap.values());
        int numExams = exams.size();
        System.out.println("Найдено экзаменов: " + numExams);
        for (Exam exam : exams) {
            System.out.println(exam + " | Студентов: " + exam.studentIds.size());
        }

        // Задаем параметры модели:
        int numDays = 3;              // Количество дней проведения экзаменов
        int numStartSlots = 16;       // Дискретизация времени: 0 соответствует 9:00, 15 — 16:30
        int examDuration = 6;         // Продолжительность экзамена = 3 часа (6 шагов по 30 минут)
        int numRooms = auditoriumList.size(); // Число аудиторий по данным файла
        int numProctors = 5;          // Фиксированное число прокторов

        System.out.println("Параметры: дней=" + numDays + ", слотов=" + numStartSlots +
                ", длительность=" + examDuration + ", аудиторий=" + numRooms +
                ", прокторов=" + numProctors);

        CpModel model = new CpModel();

        // Переменные для экзаменов:
        // examVars[i][0] – день экзамена (0 .. numDays-1)
        // examVars[i][1] – стартовый слот экзамена (0 .. numStartSlots - examDuration)
        // examVars[i][2] – индекс аудитории (0 .. numRooms-1)
        IntVar[][] examVars = new IntVar[numExams][3];
        for (int i = 0; i < numExams; i++) {
            examVars[i][0] = model.newIntVar(0, numDays - 1, "exam_" + i + "_day");
            examVars[i][1] = model.newIntVar(0, numStartSlots - examDuration, "exam_" + i + "_start");
            examVars[i][2] = model.newIntVar(0, numRooms - 1, "exam_" + i + "_room");
        }

        // Переменные для прокторов:
        IntVar[] proctorAssignment = new IntVar[numExams];
        for (int i = 0; i < numExams; i++) {
            proctorAssignment[i] = model.newIntVar(0, numProctors - 1, "proctor_" + i);
        }

        // Ограничение A: Все экзамены для одной секции проходят в один и тот же день.
        Map<String, List<Integer>> sectionToExamIndices = new HashMap<>();
        for (int i = 0; i < exams.size(); i++) {
            String sec = exams.get(i).section;
            sectionToExamIndices.computeIfAbsent(sec, k -> new ArrayList<>()).add(i);
        }
        for (Map.Entry<String, List<Integer>> entry : sectionToExamIndices.entrySet()) {
            List<Integer> examIndices = entry.getValue();
            IntVar sectionDay = model.newIntVar(0, numDays - 1, "section_" + entry.getKey() + "_day");
            System.out.println("Секция " + entry.getKey() + " экзамены: " + examIndices);
            for (int examIndex : examIndices) {
                model.addEquality(examVars[examIndex][0], sectionDay);
            }
        }

        // Ограничение B: У одного студента не может быть пересекающихся экзаменов в один день.
        Map<Integer, List<Integer>> studentToExamIndices = new HashMap<>();
        for (int i = 0; i < exams.size(); i++) {
            for (Integer studentId : exams.get(i).studentIds) {
                studentToExamIndices.computeIfAbsent(studentId, k -> new ArrayList<>()).add(i);
            }
        }
        for (Map.Entry<Integer, List<Integer>> entry : studentToExamIndices.entrySet()) {
            List<Integer> examIndices = entry.getValue();
            if (examIndices.size() > 1) {
                System.out.println("Студент " + entry.getKey() + " участвует в экзаменах: " + examIndices);
            }
            for (int i = 0; i < examIndices.size(); i++) {
                for (int j = i + 1; j < examIndices.size(); j++) {
                    int examIdx1 = examIndices.get(i);
                    int examIdx2 = examIndices.get(j);
                    BoolVar sameDay = model.newBoolVar("student_" + entry.getKey() + "_sameDay_" + examIdx1 + "_" + examIdx2);
                    model.addEquality(examVars[examIdx1][0], examVars[examIdx2][0]).onlyEnforceIf(sameDay);
                    model.addDifferent(examVars[examIdx1][0], examVars[examIdx2][0]).onlyEnforceIf(sameDay.not());

                    // Интервалы не пересекаются, если либо examVars[examIdx1][1] + examDuration <= examVars[examIdx2][1],
                    // либо examVars[examIdx2][1] + examDuration <= examVars[examIdx1][1].
                    BoolVar option1 = model.newBoolVar("opt1_" + examIdx1 + "_" + examIdx2);
                    BoolVar option2 = model.newBoolVar("opt2_" + examIdx1 + "_" + examIdx2);
                    model.addLessOrEqual(
                            LinearExpr.sum(new LinearArgument[]{ examVars[examIdx1][1], LinearExpr.constant(examDuration) }),
                            examVars[examIdx2][1]
                    ).onlyEnforceIf(option1);
                    model.addLessOrEqual(
                            LinearExpr.sum(new LinearArgument[]{ examVars[examIdx2][1], LinearExpr.constant(examDuration) }),
                            examVars[examIdx1][1]
                    ).onlyEnforceIf(option2);
                    model.addBoolOr(new Literal[]{ option1, option2 }).onlyEnforceIf(sameDay);
                }
            }
        }

        // Ограничение C: Проктор не может вести пересекающиеся экзамены (если в один день и интервалы пересекаются).
        for (int i = 0; i < numExams; i++) {
            for (int j = i + 1; j < numExams; j++) {
                BoolVar sameDay = model.newBoolVar("proctor_sameDay_" + i + "_" + j);
                model.addEquality(examVars[i][0], examVars[j][0]).onlyEnforceIf(sameDay);
                model.addDifferent(examVars[i][0], examVars[j][0]).onlyEnforceIf(sameDay.not());

                BoolVar overlap = model.newBoolVar("proctor_overlap_" + i + "_" + j);
                BoolVar noOverlap1 = model.newBoolVar("proctor_noOverlap1_" + i + "_" + j);
                BoolVar noOverlap2 = model.newBoolVar("proctor_noOverlap2_" + i + "_" + j);
                model.addLessOrEqual(
                        LinearExpr.sum(new LinearArgument[]{ examVars[i][1], LinearExpr.constant(examDuration) }),
                        examVars[j][1]
                ).onlyEnforceIf(noOverlap1);
                model.addLessOrEqual(
                        LinearExpr.sum(new LinearArgument[]{ examVars[j][1], LinearExpr.constant(examDuration) }),
                        examVars[i][1]
                ).onlyEnforceIf(noOverlap2);
                model.addBoolOr(new Literal[]{ noOverlap1, noOverlap2 }).onlyEnforceIf(overlap.not());
                model.addDifferent(proctorAssignment[i], proctorAssignment[j])
                        .onlyEnforceIf(new Literal[]{ sameDay, overlap });
            }
        }

        // Ограничение D: Если экзамены в один день и их интервалы пересекаются, аудитории должны быть различны.
        for (int i = 0; i < numExams; i++) {
            for (int j = i + 1; j < numExams; j++) {
                BoolVar sameDay = model.newBoolVar("room_sameDay_" + i + "_" + j);
                model.addEquality(examVars[i][0], examVars[j][0]).onlyEnforceIf(sameDay);
                model.addDifferent(examVars[i][0], examVars[j][0]).onlyEnforceIf(sameDay.not());

                BoolVar overlap = model.newBoolVar("room_overlap_" + i + "_" + j);
                BoolVar noOverlap1 = model.newBoolVar("room_noOverlap1_" + i + "_" + j);
                BoolVar noOverlap2 = model.newBoolVar("room_noOverlap2_" + i + "_" + j);
                model.addLessOrEqual(
                        LinearExpr.sum(new LinearArgument[]{ examVars[i][1], LinearExpr.constant(examDuration) }),
                        examVars[j][1]
                ).onlyEnforceIf(noOverlap1);
                model.addLessOrEqual(
                        LinearExpr.sum(new LinearArgument[]{ examVars[j][1], LinearExpr.constant(examDuration) }),
                        examVars[i][1]
                ).onlyEnforceIf(noOverlap2);
                model.addBoolOr(new Literal[]{ noOverlap1, noOverlap2 }).onlyEnforceIf(overlap.not());
                model.addDifferent(examVars[i][2], examVars[j][2])
                        .onlyEnforceIf(new Literal[]{ sameDay, overlap });
            }
        }

        // Решение модели
        System.out.println("Запуск решения модели...");
        CpSolver solver = new CpSolver();
        CpSolverStatus status = solver.solve(model);

        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            System.out.println("Найдено расписание экзаменов:");
            for (int i = 0; i < numExams; i++) {
                int day = (int) solver.value(examVars[i][0]);
                int startSlot = (int) solver.value(examVars[i][1]);
                int roomIdx = (int) solver.value(examVars[i][2]);
                int proctor = (int) solver.value(proctorAssignment[i]);
                int hour = 9 + (startSlot * 30) / 60;
                int minutes = (startSlot * 30) % 60;
                String time = String.format("%02d:%02d", hour, minutes);
                System.out.printf("Экзамен %s -> День: %d, Время начала: %s, Аудитория: %s, Проктор: %d%n",
                        exams.get(i), day, time, auditoriumList.get(roomIdx), proctor);
            }
        } else {
            System.out.println("Решение не найдено.");
        }
    }
}
