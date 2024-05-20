package dk.clausr.core.data.workers

//
//@HiltWorker
//class UpdateCurrentAlbumWorker @AssistedInject constructor(
//    @Assisted private val appContext: Context,
//    @Assisted workerParams: WorkerParameters,
//    private val oagRepository: OagRepository
//) : CoroutineWorker(appContext, workerParams) {
//
//    private val project = oagRepository.project
//    override suspend fun doWork(): Result {
//        val projectId = inputData.getString("PROJECT_ID") ?: return Result.failure()
//
//
////        val project = oagRepository.getProject(projectId)
//
//        return if (project == null) {
//            Result.retry()
//        } else {
//
//            Result.success(
//                Data.Builder()
//                    .putString("PROJECT", project.currentAlbum.images.maxBy { it.height }.url)
//                    .build()
//            )
//        }
//
//    }
//
//    companion object {
//        fun refreshAlbumRepeatedly(projectId: String) = PeriodicWorkRequestBuilder<UpdateCurrentAlbumWorker>(1, TimeUnit.HOURS)
//            .setInputData(Data.Builder().putString("PROJECT_ID", projectId).build())
//            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
//            .build()
//
//        fun startRefreshCurrentAlbum(projectId: String) = OneTimeWorkRequestBuilder<UpdateCurrentAlbumWorker>()
//            .setInputData(Data.Builder().putString("PROJECT_ID", projectId).build())
//            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
//            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
//            .build()
//    }
//
//}
