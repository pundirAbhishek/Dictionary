package com.example.dictionary.feature_dictionary.data.repository

import com.example.dictionary.core.Resource
import com.example.dictionary.feature_dictionary.data.local.WordInfoDao
import com.example.dictionary.feature_dictionary.data.remote.DictionaryApi
import com.example.dictionary.feature_dictionary.domain.model.WordInfo
import com.example.dictionary.feature_dictionary.domain.repository.WordInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.util.*

class WordInfoRepositoryImpl(private val api: DictionaryApi, private val dao: WordInfoDao) :
    WordInfoRepository {

    override fun getWordInfo(word: String): Flow<Resource<List<WordInfo>>> = flow {
        emit(Resource.Loading())
        val wordInfos = dao.getWordInfos(word).map { it.toWordInfo() }
        emit(Resource.Loading(data = wordInfos))

        try {
            val remoteWordInfos = api.getWordInfo(word)
            dao.deleteWordInfos(remoteWordInfos.map { it.word })
            dao.insertWordInfo(remoteWordInfos.map { it.toWordInfoEntity() })
        } catch (e: HttpException) {
            emit(Resource.Error(message = "Something went wrong!", data = wordInfos))
        } catch (e: IOException) {
            emit(Resource.Error(message = "Something went wrong!", data = wordInfos))
        }

        val newWordInfos = dao.getWordInfos(word = word).map { it.toWordInfo() }
        emit(Resource.Success(data = newWordInfos))

    }

}