package com.heroengineer.hero_engineer_backend.statistics;

import com.heroengineer.hero_engineer_backend.assignment.GradedShortAnswerAssignment;
import com.heroengineer.hero_engineer_backend.assignment.GradedShortAnswerQuestion;
import com.heroengineer.hero_engineer_backend.quest.Quest;
import com.heroengineer.hero_engineer_backend.quiz.GradedQuiz;
import com.heroengineer.hero_engineer_backend.quiz.GradedQuizQuestion;
import com.heroengineer.hero_engineer_backend.quiz.Quiz;
import com.heroengineer.hero_engineer_backend.quiz.QuizAnswer;
import com.heroengineer.hero_engineer_backend.quiz.QuizQuestion;
import com.heroengineer.hero_engineer_backend.quiz.QuizRepository;
import com.heroengineer.hero_engineer_backend.user.User;
import com.heroengineer.hero_engineer_backend.user.UserRepository;
import com.heroengineer.hero_engineer_backend.user.UserService;
import com.heroengineer.hero_engineer_backend.util.file.FileStorageService;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.ListItem;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.ListNumberingType;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for creating reports (e.g., PDF data dump for accreditation)
 */
@CrossOrigin("${origins}")
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final UserService userService;
    private final UserRepository userRepo;
    private final QuizRepository quizRepo;
    private final FileStorageService fileStorageService;

    @Autowired
    public StatisticsController(UserService userService,
                                UserRepository userRepo,
                                QuizRepository quizRepo,
                                FileStorageService fileStorageService) {
        this.userService = userService;
        this.userRepo = userRepo;
        this.quizRepo = quizRepo;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/dumpData")
    public ResponseEntity<?> downloadDataDumpInPdfReport(HttpServletRequest request) throws IOException {
        if (!userService.isProf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"You are not the professor.\"}");
        }

        List<User> allStudents = userRepo.findAll();

        String fileName = "HeroEngineer_Report.pdf";
        PdfWriter writer = fileStorageService.getNewReportsPdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4.rotate());

        document.add(new Paragraph("HeroEngineer.com Auto-Generated Report").setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(24));
        document.add(new Paragraph(""));
        document.add(new Paragraph("This report is divided into 3 sections: Total XP and Grand Challenge (GC) Ideas, In-class Assignments, and Quizzes. " +
                                           "Each student (indicated by a unique email address) is displayed once in each section."));
        document.add(new Paragraph(""));

        // Section 1: Total XP and Grand Challenge Ideas
        document.add(new Paragraph("1. Total XP and Grand Challenge (GC) Ideas").setFontSize(18));
        document.add(getTotalXPTable(allStudents));

        // Section 2: In-class Assignments
        document.add(new Paragraph(""));
        document.add(new Paragraph(""));
        document.add(new Paragraph("2. In-class Assignments").setFontSize(18));
        document.add(getInClassAssignmentsTable(allStudents));

        // Section 3: Quizzes
        document.add(new Paragraph(""));
        document.add(new Paragraph(""));
        document.add(new Paragraph("3. Quizzes").setFontSize(18));
        document.add(getQuizzesTable(allStudents));


        document.close();

        // Load file as Resource
        Resource resource = fileStorageService.loadReportsFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(resource.getFile().length())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    private Table getTotalXPTable(List<User> allStudents) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{ 5, 2, 8, 10, 10, 10 }));
        table.setWidthPercent(100);
        table.setFixedLayout();
        table.addHeaderCell(new Cell().add("Email").setTextAlignment(TextAlignment.CENTER).setFontSize(12).setBold());
        table.addHeaderCell(new Cell().add("XP").setTextAlignment(TextAlignment.CENTER).setFontSize(12).setBold());
        table.addHeaderCell(new Cell().add("Grand Challenge (GC)").setTextAlignment(TextAlignment.CENTER).setFontSize(12).setBold());
        table.addHeaderCell(new Cell().add("GC Idea 1").setTextAlignment(TextAlignment.CENTER).setFontSize(12).setBold());
        table.addHeaderCell(new Cell().add("GC Idea 2").setTextAlignment(TextAlignment.CENTER).setFontSize(12).setBold());
        table.addHeaderCell(new Cell().add("GC Idea 3").setTextAlignment(TextAlignment.CENTER).setFontSize(12).setBold());

        boolean gray = true;
        for (User student : allStudents) {
            gray = !gray;
            table.addCell(new Cell().add(student.getEmail()).setTextAlignment(TextAlignment.CENTER).setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
            table.addCell(new Cell().add(student.getXp() + "").setTextAlignment(TextAlignment.CENTER).setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));

            if (student.getGrandChallengeCategory() == null || student.getGrandChallengeCategory().isEmpty())
                table.addCell(new Cell().add("BLANK").setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
            else
                table.addCell(new Cell().add(student.getGrandChallengeCategory()).setTextAlignment(TextAlignment.CENTER).setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));

            if (student.getIdea1() == null || student.getIdea1().isEmpty())
                table.addCell(new Cell().add("BLANK").setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
            else
                table.addCell(new Cell().add(student.getIdea1()).setTextAlignment(TextAlignment.CENTER).setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));

            if (student.getIdea2() == null || student.getIdea2().isEmpty())
                table.addCell(new Cell().add("BLANK").setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
            else
                table.addCell(new Cell().add(student.getIdea2()).setTextAlignment(TextAlignment.CENTER).setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));

            if (student.getIdea3() == null || student.getIdea3().isEmpty())
                table.addCell(new Cell().add("BLANK").setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
            else
                table.addCell(new Cell().add(student.getIdea3()).setTextAlignment(TextAlignment.CENTER).setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
        }

        return table;
    }

    private Table getInClassAssignmentsTable(List<User> allStudents) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{ 2, 11 }));
        table.setWidthPercent(100);
        table.setFixedLayout();
        table.setExtendBottomRowOnSplit(true);
        table.addHeaderCell(new Cell().add("Email").setTextAlignment(TextAlignment.CENTER).setFontSize(12).setBold());
        table.addHeaderCell(new Cell().add("In-Class Assignments").setTextAlignment(TextAlignment.CENTER).setFontSize(12).setBold());

        boolean gray = true;
        for (User student : allStudents) {
            gray = !gray;
            table.addCell(new Cell().add(student.getEmail()).setTextAlignment(TextAlignment.CENTER).setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
            List<GradedShortAnswerAssignment> assignments = Optional.ofNullable(student.getGradedShortAnswerAssignments())
                                                                    .orElse(Collections.emptyList());
            Table assignmentsTable = new Table(UnitValue.createPercentArray(new float[]{ 3, 2, 5, 10 }));
            assignmentsTable.setWidthPercent(100);
            assignmentsTable.setFixedLayout();
            assignmentsTable.setExtendBottomRowOnSplit(true);
            assignmentsTable.addHeaderCell(new Cell().add("Assignment Name").setTextAlignment(TextAlignment.CENTER)
                                                     .setFontSize(9).setBold().setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER));
            assignmentsTable.addHeaderCell(new Cell().add("XP (Grade)").setTextAlignment(TextAlignment.CENTER)
                                                     .setFontSize(9).setBold().setBorderTop(Border.NO_BORDER));
            assignmentsTable.addHeaderCell(new Cell().add("Questions").setTextAlignment(TextAlignment.CENTER)
                                                     .setFontSize(9).setBold().setBorderTop(Border.NO_BORDER));
            assignmentsTable.addHeaderCell(new Cell().add("Answers").setTextAlignment(TextAlignment.CENTER)
                                                     .setFontSize(9).setBold().setBorderTop(Border.NO_BORDER).setBorderRight(Border.NO_BORDER));
            for (GradedShortAnswerAssignment assignment : assignments) {
                assignmentsTable.addCell(new Cell().add(assignment.getName())
                                                   .setTextAlignment(TextAlignment.CENTER).setFontSize(8).setBorderLeft(Border.NO_BORDER).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
                assignmentsTable.addCell(new Cell().add(assignment.getXpAwarded() + "/" + assignment.getMaxXp())
                                                   .setTextAlignment(TextAlignment.CENTER).setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));

                List<GradedShortAnswerQuestion> questions = Optional.ofNullable(assignment.getGradedQuestions())
                                                                    .orElse(Collections.emptyList());
                com.itextpdf.layout.element.List questionsList = new com.itextpdf.layout.element.List();
                questionsList.setListSymbol(ListNumberingType.DECIMAL).setFontSize(8);
                if (questions.isEmpty()) {
                    assignmentsTable.addCell(new Cell().add("BLANK").setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
                    assignmentsTable.addCell(new Cell().add("BLANK").setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
                } else {
                    com.itextpdf.layout.element.List answersList = new com.itextpdf.layout.element.List();
                    answersList.setListSymbol(ListNumberingType.DECIMAL).setFontSize(8);

                    for (GradedShortAnswerQuestion question : questions) {
                        ListItem questionListItem = new ListItem(question.getQuestion());
                        questionListItem.setFontSize(8);
                        questionsList.add(questionListItem);
                        ListItem answerListItem = new ListItem(question.getAnswer());
                        answerListItem.setFontSize(8);
                        answersList.add(answerListItem);
                    }

                    if (questionsList.isEmpty())
                        assignmentsTable.addCell(new Cell().add("BLANK").setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
                    else
                        assignmentsTable.addCell(questionsList);

                    if (answersList.isEmpty())
                        assignmentsTable.addCell(new Cell().add("BLANK").setFontSize(8).setBorderRight(Border.NO_BORDER).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
                    else
                        assignmentsTable.addCell(new Cell().add(answersList).setBorderRight(Border.NO_BORDER).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
                }
            }
            if (assignmentsTable.isEmpty())
                table.addCell(new Cell().add("BLANK").setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
            else
                table.addCell(new Cell().add(assignmentsTable).setPadding(0).setBorderTop(Border.NO_BORDER).setBorderBottom(Border.NO_BORDER).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
        }

        return table;
    }

    private Table getQuizzesTable(List<User> allStudents) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{ 2, 11 }));
        table.setWidthPercent(100);
        table.setFixedLayout();
        table.addHeaderCell(new Cell().add("Email").setTextAlignment(TextAlignment.CENTER).setFontSize(12).setBold());
        table.addHeaderCell(new Cell().add("Quizzes").setTextAlignment(TextAlignment.CENTER).setFontSize(12).setBold());

        boolean gray = true;
        for (User student : allStudents) {
            gray = !gray;
            table.addCell(new Cell().add(student.getEmail()).setTextAlignment(TextAlignment.CENTER).setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));

            List<Quest> quests = Optional.ofNullable(student.getQuests()).orElse(Collections.emptyList());
            Table quizzesTable = new Table(UnitValue.createPercentArray(new float[]{ 2, 1, 2, 10 }));
            quizzesTable.setWidthPercent(100);
            quizzesTable.setFixedLayout();
            quizzesTable.addHeaderCell(new Cell().add("Quiz Name").setTextAlignment(TextAlignment.CENTER)
                                                     .setFontSize(9).setBold().setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER));
            quizzesTable.addHeaderCell(new Cell().add("Grade").setTextAlignment(TextAlignment.CENTER)
                                                     .setFontSize(9).setBold().setBorderTop(Border.NO_BORDER));
            quizzesTable.addHeaderCell(new Cell().add("From Quest").setTextAlignment(TextAlignment.CENTER)
                                                     .setFontSize(9).setBold().setBorderTop(Border.NO_BORDER));
            quizzesTable.addHeaderCell(new Cell().add("Questions").setTextAlignment(TextAlignment.CENTER)
                                                     .setFontSize(9).setBold().setBorderTop(Border.NO_BORDER).setBorderRight(Border.NO_BORDER));
            for (Quest quest : quests) {
                for (GradedQuiz quiz : Optional.ofNullable(quest.getCompletedQuizzes()).orElse(Collections.emptyList())) {
                    // TODO: Cache global quizzes because it's super inefficient to query Mongo for every quiz for every student
                    Quiz globalQuiz = quizRepo.findById(quiz.getId()).orElse(null);

                    quizzesTable.addCell(new Cell().add(quiz.getName())
                                                       .setTextAlignment(TextAlignment.CENTER).setFontSize(8).setBorderLeft(Border.NO_BORDER).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
                    quizzesTable.addCell(new Cell().add((quiz.getGradePercent() * 100) + "%")
                                                       .setTextAlignment(TextAlignment.CENTER).setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
                    quizzesTable.addCell(new Cell().add(quest.getName())
                                                       .setTextAlignment(TextAlignment.CENTER).setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));

                    List<GradedQuizQuestion> questions = Optional.ofNullable(quiz.getQuestions())
                                                                 .orElse(Collections.emptyList());
                    Table questionsTable = new Table(UnitValue.createPercentArray(new float[]{ 1, 1 }));
                    questionsTable.setWidthPercent(100);
                    questionsTable.setFixedLayout();
                    questionsTable.addHeaderCell(new Cell().add("Question").setTextAlignment(TextAlignment.CENTER)
                                                         .setFontSize(8).setBold().setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER));
                    questionsTable.addHeaderCell(new Cell().add("Answers").setTextAlignment(TextAlignment.CENTER)
                                                         .setFontSize(8).setBold().setBorderTop(Border.NO_BORDER).setBorderRight(Border.NO_BORDER));
                    if (questions.isEmpty()) {
                        quizzesTable.addCell(new Cell().add("BLANK").setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
                    } else {

                        for (GradedQuizQuestion question : questions) {
                            questionsTable.addCell(new Cell().add(question.getQuestion())
                                                            .setTextAlignment(TextAlignment.CENTER).setFontSize(8).setBorderLeft(Border.NO_BORDER).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
                            com.itextpdf.layout.element.List answersList = new com.itextpdf.layout.element.List();
                            answersList.setListSymbol(ListNumberingType.DECIMAL).setFontSize(8);
                            for (QuizAnswer answer : question.getAnswerOptions()) {
                                String prefix = "";
                                if (answer.getId().equals(question.getStudentAnswerId())) {
                                    prefix += "[selected] ";
                                }
                                if (globalQuiz != null) {
                                    for (QuizQuestion globalQuestion : globalQuiz.getQuestionBank()) {
                                        if (globalQuestion.getId().equals(question.getId())) {
                                            for (QuizAnswer globalAnswer : globalQuestion.getAnswerOptions()) {
                                                if (globalAnswer.getId().equals(answer.getId())) {
                                                    if (globalAnswer.isCorrect()) prefix += "[correct] ";
                                                    else prefix += "[incorrect] ";
                                                    break;
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }

                                ListItem answerListItem = new ListItem(prefix + answer.getAnswer());
                                answerListItem.setFontSize(8);
                                answersList.add(answerListItem);
                            }

                            if (answersList.isEmpty())
                                questionsTable.addCell(new Cell().add("BLANK").setFontSize(8).setBorderRight(Border.NO_BORDER).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
                            else
                                questionsTable.addCell(new Cell().add(answersList).setBorderRight(Border.NO_BORDER).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
                        }

                        if (questionsTable.isEmpty())
                            quizzesTable.addCell(new Cell().add("BLANK").setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
                        else
                            quizzesTable.addCell(new Cell().add(questionsTable).setPadding(0).setBorderTop(Border.NO_BORDER).setBorderBottom(Border.NO_BORDER).setBorderRight(Border.NO_BORDER).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
                    }
                }
            }
            if (quizzesTable.isEmpty())
                table.addCell(new Cell().add("BLANK").setFontSize(8).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
            else
                table.addCell(new Cell().add(quizzesTable).setPadding(0).setBorderTop(Border.NO_BORDER).setBorderBottom(Border.NO_BORDER).setBackgroundColor(gray ? Color.LIGHT_GRAY : Color.WHITE));
        }

        return table;
    }

}
