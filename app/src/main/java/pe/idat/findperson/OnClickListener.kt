package pe.idat.findperson

interface OnClickListener {

    fun onClick(person:PersonEntity)
    fun onClickFavorite(person: PersonEntity)
    fun onClickDelete(person: PersonEntity)
}