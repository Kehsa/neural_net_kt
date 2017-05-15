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

class Config {
    var activation = ""
    var learn_rate = .0
    var regularization_rate = .0
    var train_to_test = .0f
    var net_conf = intArrayOf(0)
    var data_conf = intArrayOf(0)
    var learn_moment = .0
    var error_norm = .0

    companion object {
        fun readConfig(): Config {
            val conf: Config = Config()
            val s = Files.readAllLines(Paths.get(config_path))
            conf.activation = s[0]
            conf.learn_rate = s[1].toDouble()
            conf.regularization_rate = s[2].toDouble()
            conf.train_to_test = s[3].toFloat()
            conf.net_conf = strToIntArr(s[4])
            conf.data_conf = strToIntArr(s[5])
            conf.learn_moment = s[6].toDouble()
            conf.error_norm = s[7].toDouble()
            return conf
        }
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
            println("\nn: ${n.input}  ${n.delta}  ${n.output}")
        }
}