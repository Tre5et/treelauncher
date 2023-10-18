use std::fs;
use std::path::Path;
use actix_web::{App, HttpServer};
use once_cell::sync::OnceCell;

mod service;
mod update_manifest;
mod news;

static DATA_DIR: OnceCell<String> = OnceCell::new();
static PORT: OnceCell<u32> = OnceCell::new();

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    let path = Path::new("update.conf");
    if !path.is_file() {
        return Err(std::io::Error::new(std::io::ErrorKind::NotFound, "Config not found!"));
    }
    let contents = fs::read_to_string(path).expect("Unable to read config!");
    let lines = contents.lines();
    for l in lines {
        if l.starts_with("port=") {
            let port = l.replace("port=", "").parse::<u32>().expect("Unable to parse port!");
            PORT.set(port).expect("Unable to set port!");
        }
        if l.starts_with("data_dir=") {
            let key = l.replace("data_dir=", "");
            DATA_DIR.set(key).expect("Unable to set auth key!");
        }
    }
    if PORT.get().is_none() || DATA_DIR.get().is_none() {
        return Err(std::io::Error::new(std::io::ErrorKind::InvalidData, "Invalid config!"));
    }

    HttpServer::new(|| {
        App::new()
            .service(service::test)
            .service(service::update)
            .service(service::news)
            .service(service::version_news)
    })
    .bind(format!("0.0.0.0:{p}", p = PORT.get().expect("Port not set!")))?
    .run()
    .await
}

pub fn get_data_dir() -> String {
    return DATA_DIR.get().expect("Data dir not set!").clone();
}
