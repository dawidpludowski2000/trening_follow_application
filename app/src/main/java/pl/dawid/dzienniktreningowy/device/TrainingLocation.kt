package pl.dawid.dzienniktreningowy.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

object TrainingLocation {
    fun permitted(context:Context) = ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
    suspend fun current(context:Context):Location? {
        if(!permitted(context)) throw SecurityException("Brak uprawnienia lokalizacji")
        val manager=context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(!LocationManagerCompat.isLocationEnabled(manager)) throw IllegalStateException("Włącz lokalizację w ustawieniach telefonu.")
        val fine=ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
        val provider=if(fine && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) LocationManager.GPS_PROVIDER
            else if(manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) LocationManager.NETWORK_PROVIDER
            else throw IllegalStateException("Brak dostępnego dostawcy lokalizacji. Możesz zapisać trening bez miejsca.")
        return withTimeoutOrNull(15000L) {
            suspendCancellableCoroutine { continuation ->
                val signal=CancellationSignal()
                continuation.invokeOnCancellation {signal.cancel()}
                LocationManagerCompat.getCurrentLocation(manager,provider,signal,ContextCompat.getMainExecutor(context)) { location ->
                    if(continuation.isActive) continuation.resume(location)
                }
            }
        }
    }
}
