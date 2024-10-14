package dev.treset.treelauncher.backend.util

class EmptyingJobQueue<A>(
    private val onEmptied: () -> Unit,
    private val argumentSupplier: () -> A
) {
    private var finishProcessing = false
    private val queue: ArrayDeque<(A) -> Unit> = ArrayDeque()
    private var running = false

    init {
        Thread {
            while(true) {
                if (queue.isEmpty()) {
                    if(running) {
                        running = false
                        onEmptied()
                    }

                    if(finishProcessing)
                        break

                    Thread.sleep(100)
                } else {
                    running = true
                    queue.removeFirst()(argumentSupplier())
                }
            }
        }.start()
    }

    fun add(element: (A) -> Unit) {
        queue.add(element)
    }

    fun finish() {
        finishProcessing = true
    }
}