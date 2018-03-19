package com.kehsa.neuro

val config_path = "config.txt"
val data_text_path = "data.txt"
val data_trn_path = "data1.bin"
val data_tst_path = "data0.bin"
val net_path = "layers.bin"

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        if (args[0] == "help")
            print(
                    """Usage: app [convert] [help]
                config:
                activate function {relu, sigm}
                learning rate: 0 to 1
                regularization coef
                ratio train to test data: 0 to 1
                net config
                learn moment
                input layer size
                input + output layer size
                """)
        else if (args[0] == "convert") {
            NetKeeper.convertData()
        }
        return
    }
    javafx.application.Application.launch(FxAppWindow::class.java)
}