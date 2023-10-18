use std::fs;
use std::path::Path;
use actix_web::{get, HttpRequest, HttpResponse};
use semver::{Version, VersionReq};
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Debug)]
pub struct News {
    pub version: Option<String>,
    pub content: String
}

#[get("/news")]
pub async fn news() -> HttpResponse {
    return get_news(None);
}

#[get("/news/{version}")]
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
    return get_news(input_version);
}

fn get_news(version: Option<Version>) -> HttpResponse {
    let all_news = read_news_file();
    if all_news.is_err() {
        return all_news.unwrap_err();
    }
    let all_news = all_news.unwrap();
    let mut news_string = String::new();
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
        news_string = format!("{}\n{}", news_string, n.content);
    }

    if news_string.is_empty() {
        return HttpResponse::NoContent().finish();
    }
    return HttpResponse::Ok().body(news_string);
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