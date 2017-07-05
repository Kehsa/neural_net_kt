package main.kotlin.neuro

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Paths

fun <T> serialize(obj: T, path: String) {
    val os = Files.newOutputStream(Paths.get(path))
    val oos = ObjectOutputStream(os)
    oos.use { it.writeObject(obj) }
}

@Suppress("UNCHECKED_CAST")
fun <T> deserialize(path: String): T {
    val ins = Files.newInputStream(Paths.get(path))
    val oins = ObjectInputStream(ins)
    oins.use { return it.readObject() as T }
}

class Repeater(val max: Int) {
    var point = -1
    fun next(): Int {
        point += 1
        if (point == max) point = 0
        return point
    }
}

fun strToIntArr(str: String): IntArray {
    val arrS = str.split(',')
    val size = arrS.size
    return IntArray(size, { i -> arrS[i].toInt() })
}

fun printDoubelArr(arr: NumArr) {
    for (e in arr)
        print("$e ")
}

fun printNet(net: Net) {
    for (l in net.layers)
        for (n in l.neurons) {
            for (w in n.weights)
                print("$w ")
            println("\nn: ${n.delta}  ${n.output}")
        }
}