package mega.privacy.android.app.contacts.group

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import mega.privacy.android.app.arch.BaseRxViewModel
import mega.privacy.android.app.contacts.group.data.ContactGroupItem
import mega.privacy.android.app.contacts.usecase.GetContactGroupsUseCase
import mega.privacy.android.app.utils.LogUtil.logError
import mega.privacy.android.app.utils.notifyObserver

/**
 * ViewModel that handles all related logic to Contact Groups for the current user.
 *
 * @param getContactGroupsUseCase   UseCase to retrieve all contact groups.
 */
class ContactGroupsViewModel @ViewModelInject constructor(
    getContactGroupsUseCase: GetContactGroupsUseCase
) : BaseRxViewModel() {

    private val groups: MutableLiveData<List<ContactGroupItem>> = MutableLiveData()
    private var queryString: String? = null

    init {
        getContactGroupsUseCase.get()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { items ->
                    groups.value = items.toList()
                },
                onError = { error ->
                    logError(error.stackTraceToString())
                }
            )
            .addTo(composite)
    }

    fun getGroups(): LiveData<List<ContactGroupItem>> =
        groups.map { items ->
            if (!queryString.isNullOrBlank()) {
                items.filter { it.title.contains(queryString!!, true) }
            } else {
                items
            }
        }

    fun setQuery(query: String?) {
        queryString = query
        groups.notifyObserver()
    }
}
