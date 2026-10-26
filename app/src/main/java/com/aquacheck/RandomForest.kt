package com.aquacheck

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** One decision tree exported from scikit-learn (arrays indexed by node id). */
class Tree(
    private val feature: IntArray,
    private val threshold: DoubleArray,
    private val left: IntArray,
    private val right: IntArray,
    private val potableProb: DoubleArray,
) {
    fun predict(x: DoubleArray): Double {
        var node = 0
        while (left[node] != -1) { // -1 means leaf
            node = if (x[feature[node]] <= threshold[node]) left[node] else right[node]
        }
        return potableProb[node]
    }
}

/** Random Forest: average the trees' probability of "potable". */
class RandomForestModel(
    private val trees: List<Tree>,
    val isDemo: Boolean,
    val testAccuracy: Double?,
) {
    fun potableProbability(input: DoubleArray): Double {
        // scikit-learn compares float32 inputs, so mirror that here.
        val x = DoubleArray(input.size) { input[it].toFloat().toDouble() }
        return trees.sumOf { it.predict(x) } / trees.size
    }

    companion object {
        fun load(context: Context): RandomForestModel {
            val text = context.assets.open("rf_model.json").bufferedReader().use { it.readText() }
            val root = JSONObject(text)
            val treeArray = root.getJSONArray("trees")
            val trees = (0 until treeArray.length()).map { i ->
                val t = treeArray.getJSONObject(i)
                Tree(
                    feature = t.getJSONArray("f").toIntArray(),
                    threshold = t.getJSONArray("t").toDoubleArray(),
                    left = t.getJSONArray("l").toIntArray(),
                    right = t.getJSONArray("r").toIntArray(),
                    potableProb = t.getJSONArray("p").toDoubleArray(),
                )
            }
            return RandomForestModel(
                trees = trees,
                isDemo = root.optBoolean("is_demo", true),
                testAccuracy = if (root.isNull("test_accuracy")) null else root.getDouble("test_accuracy"),
            )
        }

        private fun JSONArray.toIntArray() = IntArray(length()) { getInt(it) }
        private fun JSONArray.toDoubleArray() = DoubleArray(length()) { getDouble(it) }
    }
}
