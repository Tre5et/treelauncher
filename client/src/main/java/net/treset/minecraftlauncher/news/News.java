package net.treset.minecraftlauncher.news;

import net.treset.mc_version_loader.json.GenericJsonParsable;
import net.treset.mc_version_loader.json.SerializationException;

import java.util.List;

public class News extends GenericJsonParsable {
    public static class NewsElement {
        private String title;
        private String content;
        private String id;

        public NewsElement(String title, String content, String id) {
            this.title = title;
            this.content = content;
            this.id = id;
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

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
    private List<NewsElement> important;
    private List<NewsElement> other;

    public News(List<NewsElement> important, List<NewsElement> other) {
        this.important = important;
        this.other = other;
    }

    public static News fromJson(String json) throws SerializationException {
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
