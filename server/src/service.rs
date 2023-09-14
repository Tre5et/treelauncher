use std::fs;
use std::path::Path;
use actix_web::{get, HttpResponse, HttpRequest, post};
use actix_web::http::header::ContentType;
use base64::Engine;
use serde::{Serialize, Deserialize};
use crate::component_type::ComponentType;
use crate::get_auth_key;

#[derive(Serialize, Deserialize)]
pub struct FileData {
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
        .json(FileData {
            path: file_path,
            content
        })
}

#[post("file/{component_type}/{id}")]
pub async fn post_file(req: HttpRequest, body: String) -> HttpResponse {
    let result = check_prerequisites(req);
    if result.is_err() {
        return result.err().unwrap();
    }
    let base_path = result.unwrap();
    let file_response = serde_json::from_str(body.as_str());
    if file_response.is_err() {
        return HttpResponse::BadRequest()
            .body(format!("Invalid file response! Error: {}", file_response.err().unwrap()));
    }
    let file_response: FileData = file_response.unwrap();
    let path = format!("{b}/{p}", b = base_path, p = file_response.path);
    let file: &Path = Path::new(path.as_str());
    if !file.is_file() {
        let prefix = file.parent().unwrap();
        fs::create_dir_all(prefix).unwrap();
        fs::File::create(file).unwrap();
    }
    let content = base64::engine::general_purpose::STANDARD.decode(file_response.content.as_bytes()).unwrap();
    let result = fs::write(file, content);
    if result.is_err() {
        return HttpResponse::InternalServerError()
            .body(format!("Unable to write file! Error: {}", result.err().unwrap()));
    }
    return HttpResponse::Ok().finish();
}

pub fn check_prerequisites(req: HttpRequest) -> Result<String, HttpResponse> {
    let key_header = req.headers().get("auth-key");
    if key_header.is_none() || key_header.unwrap() != &get_auth_key() {
        return Err(HttpResponse::Forbidden().finish());
    }

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