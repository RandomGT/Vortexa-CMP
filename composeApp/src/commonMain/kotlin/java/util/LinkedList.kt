package java.util

class LinkedList<E> {
    private val backing = mutableListOf<E>()

    fun push(element: E) {
        backing.add(0, element)
    }

    fun toList(): List<E> = backing.toList()
}
