package com.babetech.ucb_admin_access.api

import kotlinx.serialization.Serializable

@Serializable
data class Filiere(
    val id: Int,
    val shortName: String
)

@Serializable
data class Orientation(
    val id: Int,
    val title: String
)

@Serializable
data class StudentData(
    val id: Int,
    val matricule: String,
    val fullname: String,
    val active: Int,
    val avatar: String? = null,
    val noms: String? = null,
    val name: String? = null,
    val schoolFilieres: Filiere? = null,
    val schoolOrientations: Orientation? = null
)

@Serializable
data class ApiResponse(
    val message: String? = null,
    val data: StudentData? = null,
    val errors: String? = null
)
