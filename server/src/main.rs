use actix_web::{App, HttpServer};

mod service;

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    HttpServer::new(|| {
        App::new()
            .service(service::complete)
    })
    .bind("0.0.0.0:9090")?
    .run()
    .await
}