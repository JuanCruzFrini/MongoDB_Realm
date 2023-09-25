package com.cucu.mongodbrealmxml

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cucu.mongodbrealmxml.databinding.ActivityMainBinding
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.Job

class MainActivity : AppCompatActivity() {

    lateinit var realm: Realm
    lateinit var job: Job

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val config = RealmConfiguration.create(schema = setOf(Item::class))
        realm = Realm.open(config)

        setListeners()
        readDb()

        // flow.collect() is blocking -- run it in a background context
/*
        job = CoroutineScope(Dispatchers.Default).launch {
            // create a Flow from the Item collection, then add a listener to the Flow
            val itemsFlow = items.asFlow()
            itemsFlow.collect { changes: ResultsChange<Item> ->
                when (changes) {
                    // UpdatedResults means this change represents an update/insert/delete operation
                    is UpdatedResults -> {
                        changes.insertions // indexes of inserted objects
                        changes.insertionRanges // ranges of inserted objects
                        changes.changes // indexes of modified objects
                        changes.changeRanges // ranges of modified objects
                        changes.deletions // indexes of deleted objects
                        changes.deletionRanges // ranges of deleted objects
                        changes.list // the full collection of objects
                    }
                    else -> {
                        // types other than UpdatedResults are not changes -- ignore them
                    }
                }
            }
        }
*/
    }

    private fun query() {
        // items in the realm whose name begins with the letter 'D'
        val itemsThatBeginWIthD: RealmResults<Item> =
            realm.query<Item>("summary BEGINSWITH $0", "D").find()
    }

    private fun createItem() {
        realm.writeBlocking {
            copyToRealm(Item().apply {
                summary = "Do the laundry"
                isComplete = false
            })
        }

        readDb()
    }

    // all items in the realm
    private fun readDb() {
        val result = realm.query<Item>().find()
        binding.txtResult.text = ""

        val items = mutableListOf<String>()
        result.forEach {
            items.add("id:${it.id.toHexString()}\ncompleted:${it.isComplete}\nsummary:${it.summary}\n\n")
                //it.isComplete.toString())//it.toString()
        }
        binding.txtResult.text =  items.toString()//.toString()
    }

    private fun updateItem() {
        // items that have not been completed yet
        val incompleteItems: RealmResults<Item>? =
            realm.query<Item>("isComplete == false").find()

        // change the first item with open status to complete to show that the item has been done
        realm.writeBlocking {
            incompleteItems?.let { list ->
                if (list.isNotEmpty()) findLatest(list[0])?.isComplete = true
            }
        }
        readDb()
    }

    private fun setListeners() {
        binding.apply {
            btnCreate.setOnClickListener {
                createItem()
            }
            btnRead.setOnClickListener {
                readDb()
            }
            btnUpdate.setOnClickListener {
                updateItem()
            }
            btnDelete.setOnClickListener {
                deleteItem()
            }
        }
    }

    private fun deleteItem() {
        // delete the first item in the realm
        realm.writeBlocking {
            val writeTransactionItems = query<Item>().find()
            if (writeTransactionItems.isNotEmpty()) delete(writeTransactionItems.first())
        }
        readDb()
    }

    override fun onDestroy() {
        job.cancel()
        realm.close()
        super.onDestroy()
    }
}