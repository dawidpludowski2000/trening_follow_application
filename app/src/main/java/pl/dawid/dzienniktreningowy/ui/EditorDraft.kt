package pl.dawid.dzienniktreningowy.ui

import org.json.JSONArray
import org.json.JSONObject
import pl.dawid.dzienniktreningowy.domain.ExerciseInput

/** Kompaktowy zapis formularza do SavedStateHandle, również na odtworzenie procesu. */
data class EditorDraft(val kind: String = "plan", val id: Long = 0, val name: String = "",
    val date: Long = System.currentTimeMillis(), val duration: String = "45", val note: String = "",
    val latitude: Double? = null, val longitude: Double? = null,
    val exercises: List<ExerciseInput> = listOf(ExerciseInput())) {
    fun encode(): String = JSONObject().apply {
        put("kind",kind); put("id",id); put("name",name); put("date",date); put("duration",duration); put("note",note)
        latitude?.let { put("latitude",it) }; longitude?.let { put("longitude",it) }
        put("exercises", JSONArray().apply { exercises.forEach { e -> put(JSONObject().apply {
            put("name",e.name); put("sets",e.sets); put("reps",e.reps); put("weight",e.weight)
        }) } })
    }.toString()
    companion object {
        fun decode(value: String): EditorDraft = JSONObject(value).let { j ->
            val items=j.getJSONArray("exercises")
            EditorDraft(j.getString("kind"), j.getLong("id"), j.getString("name"),j.getLong("date"),j.getString("duration"),j.getString("note"),
                if(j.has("latitude")) j.getDouble("latitude") else null, if(j.has("longitude")) j.getDouble("longitude") else null,
                (0 until items.length()).map { i -> items.getJSONObject(i).let { ExerciseInput(it.getString("name"),it.getString("sets"),it.getString("reps"),it.getString("weight")) } })
        }
    }
}
