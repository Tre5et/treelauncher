use std::fs;
use std::path::Path;
use actix_web::{get, HttpResponse, HttpRequest};
use actix_web::http::header::ContentType;
use base64::Engine;
use serde::{Serialize, Deserialize};
use crate::component_type::ComponentType;

#[derive(Serialize, Deserialize)]
pub struct FileResponse {
    path: String,
    content: String
}

#[get("/complete/{component_type}/{id}")]
pub async fn complete(req: HttpRequest) -> HttpResponse {
    let result = check_prerequisites(req);
    if result.is_err() {
        return result.err().unwrap();
    }
    return HttpResponse::Ok()
        .content_type(ContentType::json()).finish();
}

#[get("/file/{component_type}/{id}/{file_path:.*}")]
pub async fn get_file(req: HttpRequest) -> HttpResponse {
    let file_path = req.match_info().query("file_path").parse::<String>().unwrap_or("".to_string());
    let result = check_prerequisites(req);
    if result.is_err() {
        return result.err().unwrap();
    }
    let base_path = result.unwrap();
    let path = format!("{b}/{p}", b = base_path, p = file_path);
    let file: &Path = Path::new(path.as_str());
    if !file.is_file() {
        return HttpResponse::BadRequest()
            .body(format!("Invalid file path!"));
    }
    let content = base64::engine::general_purpose::STANDARD.encode(fs::read(file).expect("Unable to read file!"));
    return HttpResponse::Ok()
        .content_type(ContentType::json())
        .json(FileResponse {
            path: file_path,
            content
        })
}

pub fn check_prerequisites<'a>(req: HttpRequest) -> Result<String, HttpResponse> {
    let component_type = req.match_info().query("component_type").parse::<ComponentType>();
    if component_type.is_err() {
        return Err(HttpResponse::BadRequest()
            .body(format!("Invalid component type!")));
    }
    let id = req.match_info().query("id").parse::<String>();
    if id.is_err() {
        return Err(HttpResponse::BadRequest()
            .body(format!("Invalid id!")));
    }
    let id = id.unwrap();
    let path: String = format!("data/{t}/{i}", t = component_type.unwrap(), i = id);
    let dir: &Path = Path::new(path.as_str());
    if !dir.is_dir() {
        return Err(HttpResponse::BadRequest()
            .body(format!("Component doesn't exist! Try /get/{t}/{i} to create it.", t = component_type.unwrap(), i = id)));
    }
    return Ok(path);
}