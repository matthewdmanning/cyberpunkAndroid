import android.graphics.RenderEffect
import android.graphics.BlendMode
fun test() {
    val e1 = RenderEffect.createOffsetEffect(0f, 0f)
    val e2 = RenderEffect.createOffsetEffect(0f, 0f)
    val e3 = RenderEffect.createBlendModeEffect(e1, e2, BlendMode.DST_OVER)
}
