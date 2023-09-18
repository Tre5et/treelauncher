use std::fs;
use std::path::Path;
use actix_web::{App, HttpServer, web};
use once_cell::sync::OnceCell;

mod service;
mod component_type;

static AUTH_KEY: OnceCell<String> = OnceCell::new();
static PORT: OnceCell<u32> = OnceCell::new();


#[actix_web::main]
async fn main() -> std::io::Result<()> {
    let path = Path::new("config.sync");
    if !path.is_file() {
        return Err(std::io::Error::new(std::io::ErrorKind::NotFound, "Config not found!"));
    }
    let contents = fs::read_to_string(path).expect("Unable to read auth key!");
    let lines = contents.lines();
    for l in lines {
        if l.starts_with("port=") {
            let port = l.replace("port=", "").parse::<u32>().expect("Unable to parse port!");
            PORT.set(port).expect("Unable to set port!");
        }
        if l.starts_with("auth_key=") {
            let key = l.replace("auth_key=", "");
            AUTH_KEY.set(key).expect("Unable to set auth key!");
        }
    }
    if PORT.get().is_none() || AUTH_KEY.get().is_none() {
        return Err(std::io::Error::new(std::io::ErrorKind::InvalidData, "Invalid config!"));
    }

    HttpServer::new(|| {
        App::new()
            .app_data(web::PayloadConfig::default()
                .limit(1024 * 1024 * 1024 * 10)
            )
            .service(service::test)
            .service(service::all)
            .service(service::new)
            .service(service::get)
            .service(service::get_file)
            .service(service::post_file)
            .service(service::complete)
            .service(service::hash)
    })
    .bind(format!("0.0.0.0:{p}", p = PORT.get().expect("Port not set!")))?
    .run()
    .await
}

pub fn get_auth_key() -> String {
    return AUTH_KEY.get().expect("Auth key not set!").clone();
}
