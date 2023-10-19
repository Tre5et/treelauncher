package net.treset.minecraftlauncher.news;

import net.treset.mc_version_loader.json.GenericJsonParsable;

import java.util.List;

public class News extends GenericJsonParsable {
    public static class NewsElement {
        private String title;
        private String content;

        public NewsElement(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
    private List<NewsElement> important;
    private List<NewsElement> other;

    public News(List<NewsElement> important, List<NewsElement> other) {
        this.important = important;
        this.other = other;
    }

    public static News fromJson(String json) {
        return fromJson(json, News.class);
    }

    public List<NewsElement> getImportant() {
        return important;
    }

    public void setImportant(List<NewsElement> important) {
        this.important = important;
    }

    public List<NewsElement> getOther() {
        return other;
    }

    public void setOther(List<NewsElement> other) {
        this.other = other;
    }
}
