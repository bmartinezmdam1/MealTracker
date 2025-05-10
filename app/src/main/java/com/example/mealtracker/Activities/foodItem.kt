import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FoodItem(
    val id: Long = 0L,
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val vitaminA: Int,
    val vitaminC: Int,
    val totalGrams: Int
) : Parcelable
