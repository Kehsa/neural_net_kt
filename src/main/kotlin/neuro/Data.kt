package main.kotlin.neuro

import java.io.Serializable

class Data(): Serializable {
    val input = mutableListOf<NumArr>()
    val output = mutableListOf<NumArr>()
    constructor(strs: List<String>, isize: Int, columnsCount: Int) : this() {
        val osize = columnsCount - isize
        val shift = isize

        for (s in strs) {
            if (s == "")
                break
            val cl = s.split(',')
            if (cl.size != columnsCount) {
                throw Throwable("Bad data clsize=${cl.size} colcnt=$columnsCount is=$isize os=$osize sh=$shift")
            }
            val id = NumArr(isize)
            val od = NumArr(osize)
            for ((i, c) in cl.withIndex()) {
                if (i < isize) {
                    id[i] = c.toFloat()
                } else {
                    od[i - shift] = c.toFloat()
                }
            }
            input.add(id)
            output.add(od)
        }
    }
}