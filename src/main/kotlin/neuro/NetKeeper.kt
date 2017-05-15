package main.kotlin.neuro

import javafx.application.Platform
import javafx.concurrent.Service
import javafx.concurrent.Task
import java.lang.ref.WeakReference
import java.nio.file.Files
import java.nio.file.Paths

class NetKeeper(val window: WeakReference<FxAppWindow>): Service<Unit>() {
    var conf = Config.readConfig()
    var need_print = false
    var debug = false
    var test_only = false
    var stepEpochs = 10
    var i = 0

    private var dataTrain = Data()
    private var dataTest = Data()
    private var repTrain = Repeater(0)
    private var repTest = Repeater(0)
    private var net: Net? = null

    init {
        loadData()
    }

    override fun createTask(): Task<Unit> {
        return object : Task<Unit>() {
            override fun call() {
                if (net == null) {
                    loadData()
                    buildNet()
                }
                while (!isCancelled) {
                    i += 1
                    if (!test_only)
                        step(net!!, dataTrain.input, dataTrain.output)
                    if (i % stepEpochs == 0)
                        test()
                }
            }
        }
    }

    fun test() {
        val net = net!!
        val resTrn = net.mse(dataTrain.input, dataTrain.output)
        val resTst = net.mse(dataTest.input, dataTest.output)
        if (need_print) {
            if (debug)
                printNet(net)
            println("train  mse= ${resTrn.first}, ${resTrn.second}%")
            println("test mse= ${resTst.first}, ${resTst.second}%")
            val trainI = repTrain.next()
            val trainRes = net.process(dataTrain.input[trainI])
            printDoubelArr(dataTrain.output[trainI])
            println()
            printDoubelArr(trainRes)
            println("\n$i")
            val testI = repTest.next()
            val testRes = net.process(dataTest.input[testI])
            printDoubelArr(dataTest.output[testI])
            println()
            printDoubelArr(testRes)
            println("\n========================================================")
        }
        Platform.runLater {
            window.get()!!.updateCharts(i, resTrn.first, resTst.first, resTrn.second, resTst.second)
        }
    }


    fun loadConf() {
        conf = Config.readConfig()
    }

    fun loadData() {
        if (!Files.exists(Paths.get(data_trn_path)) || !Files.exists(Paths.get(data_tst_path)))
            convertData()
        dataTrain = deserialize(data_trn_path)
        dataTest = deserialize(data_tst_path)
        repTrain = Repeater(dataTrain.input.size)
        repTest = Repeater(dataTest.input.size)
    }

    fun buildNet() {
        i = 0 //val (f, fd) = ActivationFun.getFunc(conf.activation)
        net = Net(conf.net_conf, conf.learn_rate, conf.regularization_rate, conf.learn_moment, conf.error_norm)
    }

    fun step(net: Net, tri: List<NumArr>, tro: List<NumArr>) {
        for (i in tri.indices) {
            net.calc(tri[i])
            net.backpropagation(tro[i])
        }
    }

    companion object {
        fun convertData() {
            val strings = Files.readAllLines(Paths.get(data_text_path))
            val conf = Config.readConfig()
            val columns = conf.data_conf
            val columnsCount = columns.size
            var isize = 0
            for ((i, v) in columns.withIndex()) {
                if (v == 0) {
                    isize = i
                    break
                }
            }
            val (train, test) = dataFromStringList(strings, conf.train_to_test, isize, columnsCount)
            serialize(train, data_trn_path)
            serialize(test, data_tst_path)
        }
        fun dataFromStringList(str: List<String>, trainRate: Float,
                               isize: Int, columnsCount: Int): Pair<Data, Data> {
            println(str.size)
            println(trainRate)
            val spike = (str.size * trainRate).toInt()
            if (spike < 1 || spike >= str.size-1) throw Throwable("err spike=$spike")
            println("spike $spike")
            return Pair(Data(str.subList(0, spike), isize, columnsCount),
                    Data(str.subList(spike, str.lastIndex), isize, columnsCount))
        }
    }

    fun saveNet() {
        if (net != null)
            serialize(net, net_path)
    }
    fun loadNet() {
        net = deserialize(net_path)
    }
}