package com.example.sos_app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ContactList(private var context:Context, private var contacts: List<Contact>):RecyclerView.Adapter<ContactList.ContactViewHolder>() {
    class ContactViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)

    private lateinit var contactName : TextView;
    private lateinit var phoneNo : TextView;
    private lateinit var btnEditContact: Button;

    var db = DbHelper(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.contact_item,
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        var currContact = contacts[position];
        holder.itemView.apply {
            contactName = findViewById(R.id.tvName);
            phoneNo = findViewById(R.id.tvPhoneNo);
            btnEditContact = findViewById(R.id.btnDeleteContact);
            contactName.text = currContact.getName();
            phoneNo.text = currContact.getPhoneNo();
            btnEditContact.setOnClickListener{
                MaterialAlertDialogBuilder(context)
                    .setTitle("Remove Contact")
                    .setMessage("Are you sure want to remove this contact?")
                    .setPositiveButton("YES") { _, _ ->
                        db.deleteContact(currContact)
                        contacts = db.getAllContacts();
                        notifyDataSetChanged()
                        Toast.makeText(context, "Contact removed!", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(
                        "NO"
                    ) { _, _ -> }
                    .show()
            }
        }
    }

    override fun getItemCount(): Int {
        return contacts.size;
    }

    fun refresh(contactsList:List<Contact>){
        contacts = contactsList;
        notifyDataSetChanged();
    }
}