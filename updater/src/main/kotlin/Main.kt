import java.io.IOException

fun main() {
    println("Hello World!")

    try {
        readUpdate()
    } catch (e: IOException) {
        println(e)
    }
}