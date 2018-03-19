package com.kehsa.neuro

import java.nio.file.Files
import java.nio.file.Paths

class Config {
    var activation = ""
    var learn_rate = .0f
    var regularization_rate = .0f
    var train_to_test = .0f
    var net_conf = intArrayOf(0)
    var data_in = 0
    var data_all = 0
    var learn_moment = .0f

    companion object {
        fun readConfig(): Config {
            val conf: Config = Config()
            val s = Files.readAllLines(Paths.get(config_path))
            conf.activation = s[0]
            conf.learn_rate = s[1].toFloat()
            conf.regularization_rate = s[2].toFloat()
            conf.train_to_test = s[3].toFloat()
            conf.net_conf = strToIntArr(s[4])
            conf.learn_moment = s[5].toFloat()
            conf.data_in = s[6].toInt()
            conf.data_all = s[7].toInt()
            return conf
        }
    }
}