package chat.rocket.android.push

import androidx.core.os.bundleOf
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import chat.rocket.android.push.worker.TokenRegistrationWorker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import javax.inject.Inject

class FirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var pushManager: PushManager

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // XXX - for now this is ok, if we start to do network calls, use a Worker instead
        message.data?.let {
            pushManager.handle(bundleOf(*(it.map { Pair(it.key, it.value) }).toTypedArray()))
        }
    }

    override fun onNewToken(token: String) {
        val data = workDataOf("token" to token)
        val constraint =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val work = OneTimeWorkRequestBuilder<TokenRegistrationWorker>()
                .setInputData(data)
                .setConstraints(constraint)
                .build()

        // Schedule a job since we are using network...
        WorkManager.getInstance().enqueue(work)
    }
}