package com.heroengineer.hero_engineer_backend.herocouncil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB representation of a Hero Council.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HeroCouncil {

    @Id
    public String id;
    public String name;
    public List<String> emails; // Emails of students in this Hero Council
    public boolean approved; // Whether or not the professor has approved this Hero Council
    public String declarationFileName;
    public List<Announcement> announcements;
    public List<QuestInfo> questInfos; // Info about each quest that is specific to this Hero Council (e.g. a quest code)

    public HeroCouncil(String name, List<String> emails, boolean approved, String declarationFileName) {
        this.name = name;
        this.emails = emails;
        this.approved = approved;
        this.declarationFileName = declarationFileName;
        this.announcements = new ArrayList<>();
        this.questInfos = new ArrayList<>();
    }

    public String getCodeForQuestId(String questId) {
        if (questId == null || getQuestInfos() == null) return "";
        for (QuestInfo questInfo : getQuestInfos()) {
            if (questId.equals(questInfo.getQuestId())) {
                return questInfo.getCode();
            }
        }
        return "";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Announcement {
        public int num; // Used for ordering -- to display announcements in ascending order on frontend
        public String text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestInfo {
        public String questId;
        public String code;
    }

}
