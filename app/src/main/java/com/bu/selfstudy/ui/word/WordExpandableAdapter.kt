package com.bu.selfstudy.ui.word

import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter

class WordExpandableAdapter : BaseExpandableListAdapter() {
    override fun getGroupCount(): Int = 4

    override fun getChildrenCount(groupPosition: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getGroup(groupPosition: Int): Any {
        TODO("Not yet implemented")
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        TODO("Not yet implemented")
    }

    override fun getGroupId(groupPosition: Int): Long {
        TODO("Not yet implemented")
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        TODO("Not yet implemented")
    }

    override fun hasStableIds(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        TODO("Not yet implemented")
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        TODO("Not yet implemented")
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        TODO("Not yet implemented")
    }
}