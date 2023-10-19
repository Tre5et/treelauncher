use std::fs;
use std::path::Path;
use actix_web::{get, HttpRequest, HttpResponse};
use semver::{Version, VersionReq};
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Clone, Debug)]
struct News {
    version: Option<String>,
    title: Vec<LocalItem>,
    content: Option<Vec<LocalItem>>,
    important: Option<bool>
}

#[derive(Serialize, Deserialize, Clone, Debug)]
struct LocalItem {
    locale: String,
    content: String
}

#[derive(Serialize, Deserialize, Debug)]
struct NewsOut {
    title: String,
    content: Option<String>
}

#[derive(Serialize, Deserialize, Debug)]
struct NewsContent {
    important: Option<Vec<NewsOut>>,
    other: Option<Vec<NewsOut>>
}

#[get("/news/{locale}")]
pub async fn news(req: HttpRequest) -> HttpResponse {
    let locale = req.match_info().get("locale");
    if locale.is_none() {
        return HttpResponse::BadRequest().body("Locale not specified!");
    }
    return get_news(None, Some(locale.unwrap().to_string()));
}

#[get("/news/{version}/{locale}")]
pub async fn version_news(req: HttpRequest) -> HttpResponse {
    let input_version = req.match_info().get("version");
    if input_version.is_some() {
        let input_version = input_version.unwrap();
        let parsed_version = Version::parse(input_version);
        if parsed_version.is_err() {
            return HttpResponse::BadRequest().body("Invalid version!");
        }
    }
    let input_version = if input_version.is_some() { Some(Version::parse(input_version.unwrap()).unwrap()) } else { None };
    let locale = req.match_info().get("locale");
    if locale.is_none() {
        return HttpResponse::BadRequest().body("Locale not specified!");
    }
    return get_news(input_version, Some(locale.unwrap().to_string()));
}

fn get_news(version: Option<Version>, locale: Option<String>) -> HttpResponse {
    let all_news = read_news_file();
    if all_news.is_err() {
        return all_news.unwrap_err();
    }
    let all_news = all_news.unwrap();
    let mut important: Vec<NewsOut> = Vec::new();
    let mut other: Vec<NewsOut> = Vec::new();
    for n in all_news {
        if n.version.is_some() {
            if version.is_none() {
                continue;
            }
            let requires = VersionReq::parse(n.version.unwrap().as_str());
            if requires.is_err() {
                continue;
            }
            if !requires.unwrap().matches(&version.clone().unwrap()) {
                continue;
            }
        }
        let title = n.title.clone().iter()
            .find(|x| x.locale == locale.clone().unwrap_or("en-us".to_string()))
            .unwrap_or(n.title.get(0).unwrap())
            .clone().content;
        let content = if n.content.is_some()
            {
                Some(
                    n.content.clone().unwrap().iter()
                    .find(|x| x.locale == locale.clone().unwrap_or("en-us".to_string()))
                    .unwrap_or(n.content.unwrap().get(0).unwrap())
                    .clone().content
                )
            } else {
                None
            };

        if n.important.is_some() && n.important.unwrap() {
            important.push(NewsOut {
                title,
                content
            })
        } else {
            other.push(NewsOut {
                title,
                content
            });
        }
    }

    return HttpResponse::Ok().json(
        NewsContent {
            important: if important.is_empty() { None } else { Some(important) },
            other: if other.is_empty() { None } else { Some(other) }
        }
    );
}

fn read_news_file() -> Result<Vec<News>, HttpResponse> {
    let data_dir = crate::get_data_dir();
    let news_file = format!("{}/news.json", data_dir.as_str());
    let path = Path::new(news_file.as_str());
    if !path.is_file() {
        return Err(HttpResponse::InternalServerError().body("News file not found!"));
    }
    let news_contents = fs::read_to_string(path);
    if news_contents.is_err() {
        return Err(HttpResponse::InternalServerError().body("Unable to read news file!"));
    }
    let news_contents = news_contents.unwrap();

    let news_content: Result<Vec<News>,_> = serde_json::from_str(news_contents.as_str());
    if news_content.is_err() {
        return Err(HttpResponse::InternalServerError().body("Unable to parse news file!"));
    }
    return Ok(news_content.unwrap());
}