package main.kotlin.neuro

import javafx.application.Application
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Side
import javafx.scene.Scene
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.lang.ref.WeakReference
import kotlin.system.exitProcess

class FxAppWindow : Application() {
    var graf_mse_trn = XYChart.Series<Number, Number>()
    var graf_mse_tst = XYChart.Series<Number, Number>()
    var graf_pc_trn = XYChart.Series<Number, Number>()
    var graf_pc_tst = XYChart.Series<Number, Number>()
    val label = Label()
    val keeper = NetKeeper(WeakReference(this))

    init {
        graf_mse_trn.name = "train"
        graf_mse_tst.name = "test"
        graf_pc_trn.name = "train"
        graf_pc_tst.name = "test"
    }

    fun clear() {
        graf_mse_trn.data.clear()
        graf_mse_tst.data.clear()
        graf_pc_trn.data.clear()
        graf_pc_tst.data.clear()
    }

    private fun buildLineChart(title: String, y_label: String, df: XYChart.Series<Number, Number>,
                               ds: XYChart.Series<Number, Number>): Tab {
        val xAxis = NumberAxis()
        val yAxis = NumberAxis()
        xAxis.label = "Epoch"
        yAxis.label = y_label

        val lineChart = LineChart(xAxis, yAxis)
        val n = 200.0
        val x = 1200.0
        lineChart.setMinSize(n,n)
        lineChart.setMaxSize(x,x)

        lineChart.title = title
        lineChart.data.add(df)
        lineChart.data.add(ds)
        return Tab(y_label, lineChart)
    }

    private fun buildButton(title: String, handler: (ActionEvent)->Unit ): Button {
        val btn = Button(title)
        btn.onAction = EventHandler(handler)
        return btn
    }

    private fun buildTxtField(): TextField {
        return object : TextField() {
            override fun replaceText(start: Int, end: Int, text: String) {
                if (!text.matches("[a-z, A-Z]".toRegex())) {
                    super.replaceText(start, end, text)
                }
                label.text = "Enter a numeric value"
            }
            override fun replaceSelection(text: String) {
                if (!text.matches("[a-z, A-Z]".toRegex())) {
                    super.replaceSelection(text)
                }
            }
        }
    }

    fun updateCharts(i: Int, mtr: Num, mts: Num, ptr: Num, pts: Num) {
        graf_mse_trn.data.add(XYChart.Data<Number, Number>(i, mtr))
        graf_mse_tst.data.add(XYChart.Data<Number, Number>(i, mts))
        graf_pc_trn.data.add(XYChart.Data<Number, Number>(i, ptr))
        graf_pc_tst.data.add(XYChart.Data<Number, Number>(i, pts))
    }

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Neural Net (Lab 3)"
        setUserAgentStylesheet(STYLESHEET_MODENA)

        val topPane = HBox()
        topPane.children.add(buildButton("New Net", { keeper.buildNet()
            clear()
            label.text = "Net create"}))
        topPane.children.add(buildButton("Save Net", { keeper.saveNet()
            label.text = "Net saved"}))
        topPane.children.add(buildButton("Load Net", { keeper.loadNet()
            clear()
            label.text = "Net loaded"}))
        topPane.children.add(buildButton("Convert Data", { NetKeeper.convertData()
            label.text = "Data converted"}))
        topPane.children.add(buildButton("Load Data", { keeper.loadData()
            clear()
            label.text = "Data loaded"}))
        topPane.children.add(buildButton("Reload Config", { keeper.loadConf()
            label.text = "Config reloaded"}))
        topPane.children.add(buildButton("Clear", { clear() }))

        val chartsPane = TabPane()
        chartsPane.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        chartsPane.side = Side.BOTTOM
        chartsPane.tabs.add(buildLineChart("Mean squared error", "MSE", graf_mse_trn, graf_mse_tst))
        chartsPane.tabs.add(buildLineChart("Percent of right answer", "Percent", graf_pc_trn, graf_pc_tst))

        val sidePane = VBox()
        sidePane.children.add(buildButton("Learn", { keeper.restart()
            label.text = "Learning"}))
        sidePane.children.add(buildButton("Stop", { keeper.cancel()
            label.text = "Learning stopped"}))

        var txtf = buildTxtField()
        txtf.promptText = keeper.conf.learn_rate.toString()
        sidePane.children.add(txtf)
        sidePane.children.add(buildButton("Apply learn rate", {
            keeper.conf.learn_rate = txtf.text.toDouble()
            label.text = "New learn rate is ${keeper.conf.learn_rate}"
        }))

        txtf = buildTxtField()
        txtf.promptText = keeper.conf.regularization_rate.toString()
        sidePane.children.add(txtf)
        sidePane.children.add(buildButton("Apply regul. rate", {
            keeper.conf.regularization_rate = txtf.text.toDouble()
            label.text = "New regularization rate is ${keeper.conf.regularization_rate}"
        }))

        txtf = buildTxtField()
        txtf.promptText = keeper.stepEpochs.toString()
        sidePane.children.add(txtf)
        sidePane.children.add(buildButton("Apply epoch step", {
            keeper.stepEpochs = txtf.text.toInt()
            label.text = "New step is ${keeper.stepEpochs}"
        }))

        var cb =  CheckBox("Print to stdout")
        cb.selectedProperty().addListener { _,_, new_val -> keeper.need_print = new_val }
        sidePane.children.add(cb)
        cb =  CheckBox("Debug (print layers)")
        cb.selectedProperty().addListener { _,_, new_val -> keeper.debug = new_val }
        sidePane.children.add(cb)
        cb =  CheckBox("Test only")
        cb.selectedProperty().addListener { _,_, new_val -> keeper.test_only = new_val }
        sidePane.children.add(cb)

        val btnExit = buildButton("Exit", { exitProcess(0) })
        sidePane.children.add(btnExit)
        val bp = BorderPane(chartsPane,topPane,null,label,sidePane)

        val scene = Scene(bp)
        scene.stylesheets.add("style.css")
        primaryStage.scene = scene
        primaryStage.show()
    }
}