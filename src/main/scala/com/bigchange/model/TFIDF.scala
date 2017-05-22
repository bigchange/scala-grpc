package com.bigchange.model

/**
  * Created by Jerry on 2017/5/18.
  */
import java.io.{File => JavaFile}

import breeze.linalg.{SparseVector, norm}
import com.hankcs.hanlp.HanLP
import org.apache.spark.mllib.feature.{HashingTF, IDF, IDFModel}
import org.apache.spark.mllib.linalg.{Vector, SparseVector => SV}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer
import scala.reflect.io.File

object TFIDF extends Serializable {

  private var model: TFIDF = null

  def apply(sc: SparkContext, originData:String, formatData: String): TFIDF = {
    if (model == null)
      model = new TFIDF(sc, originData, formatData)
    model

  }

  def getInstance = model

}
/**
  * Created by Administrator on 2016/1/14.
  *  spark mllib 中的tf-idf 算法计算文档相似度
  *  originData : 需要训练的数据的原始map格式（txt, id）
  *  formatData: 用来训练的数据格式（text split by ","， id）
  */
class TFIDF (sc: SparkContext, originData:String, formatData: String) extends Serializable {


  type tfidfModel = (IDFModel, RDD[(Long, Vector)])

  val result = new ListBuffer[(String, String, Double)]

  val trainDataPath = originData

  val normalDataPath = formatData

  val dataMap = getDataMap(originData)

  val model: tfidfModel = gettfidfModel(normalDataPath)

  def hashingTF(vSize: Int) = {
    new HashingTF(Math.pow(2, vSize).toInt)
  }

  /**
    * 分词
    * 得分到关键分词的结果
    * 得到关键字
    * 得到关键短语
    * 用来得到这个txt的tfidf向量值
    * 计算相似度
    * @param txt
    * @return
    */
  def segment(txt:String) = {
    val lb = new ListBuffer[String]
    val keyWords = HanLP.extractKeyword(txt, 8)
    val phrases = HanLP.extractPhrase(txt, 8)
    keyWords.addAll(phrases)
    // val list = HanLP.segment(txt)
    val size = keyWords.size()
    for (i <- 0 to size - 1) {
      val item = keyWords.get(i)
      lb.+=(item)
    }
    val list = HanLP.segment(txt)
    for (j <- 0 to list.size() - 1) {
      val item = list.get(j).word
      lb.+=(item)
    }

    lb.distinct
  }

  /**
    * 用一个职位去对比其中与哪个最相似
    * 只取top5观看匹配相似度结果
    */
  def cosSimilarity(x: String) = {

    val iDF = generateFeatrues(x)
    val sv1 = iDF.asInstanceOf[SV]
    val bsv1 = new SparseVector[Double](sv1.indices, sv1.values, sv1.size)
    val ret = model._2.map {
      case (id2, idf2) =>
        val mapId2Value = dataMap.get(id2).getOrElse("None")
        val sv2 = idf2.asInstanceOf[SV]
        val bsv2 = new SparseVector[Double](sv2.indices, sv2.values, sv2.size)
        val cosSim = bsv1.dot(bsv2) / (norm(bsv1) * norm(bsv2))
        (x, mapId2Value, cosSim)
    }
    val top5 = ret.top(10)(ord = Ordering.by(_._3))
    println("====== top 10 sim ======")
    top5.foreach(println)
  }

  /**
    * 模型内部训练数据相似性对比
    * @param idAndTFIDFVector
    */
  def cosSimilarity(idAndTFIDFVector: RDD[(Long, Vector)]) = {
    // broadcast tf-idf vectors
    val idAndTFIDFVectorBroadCast = sc.broadcast(idAndTFIDFVector.collect())
    // cal doc cosineSimilarity
    val docSims = idAndTFIDFVector.flatMap {
      case (id1, idf1) =>
        // filter the same doc id
        val idfs = idAndTFIDFVectorBroadCast.value.filter(_._1 != id1)
        val sv1 = idf1.asInstanceOf[SV]
        import breeze.linalg._
        val bsv1 = new SparseVector[Double](sv1.indices, sv1.values, sv1.size)
        idfs.map {
          case (id2, idf2) =>
            val sv2 = idf2.asInstanceOf[SV]
            val bsv2 = new SparseVector[Double](sv2.indices, sv2.values, sv2.size)
            val cosSim = bsv1.dot(bsv2) / (norm(bsv1) * norm(bsv2))
            (id1, id2, cosSim)
        }
    }
    docSims.foreach(println)
  }

  /**
    * featrue 生成
    * @return
    */
  def generateFeatrues(txt:String) = {
    // feature number
    val hashingTF = new HashingTF(Math.pow(2, 18).toInt)
    val tf = hashingTF.transform(segment(txt))
    val idf = model._1.transform(tf)
    idf
  }

  /**
    * data mapping
    */
  def getDataMap(file: String) = {
    // 保存职位的map（训练tdidf模型的之前要确保map是最新的）
    val tdata = sc.textFile(file).map(_.split("\t")).map(x => (x(1).toLong + 1, x(0)))
    val trData = tdata.mapValues(x => x.toLowerCase)
    trData.collectAsMap()
  }

  /**
    * 按照结果集 职位名称数据集
    * 训练tfidf模型
    * @param data
    * @return
    */
  def trainModel(data: RDD[(Seq[String], Long)]) = {

    // feature number
    val hashingTF = new HashingTF(Math.pow(2, 18).toInt)

    val idAndTFVector = data.map { case (seq, num) =>
      val tf = hashingTF.transform(seq)
      (num, tf)
    }
    idAndTFVector.cache()
    // build idf model
    val idf = new IDF().fit(idAndTFVector.values)
    // transform tf vector to tf-idf vector
    val idAndTFIDFVector = idAndTFVector.mapValues(v => idf.transform(v))
    idAndTFIDFVector.cache()

    val model =(idf, idAndTFIDFVector)

    model

  }

  /**
    * 加载训练数据集
    * @param file
    * @return
    */
  def loadTrainData(file:String) = {
    val trainData = file
    val src = sc.textFile(trainData).map(_.split("\t")(0)).map(_.toLowerCase)
    val dataSort= src.distinct().zipWithIndex().sortBy(_._2, ascending= true, numPartitions = 1)
    dataSort
  }

  /**
    * 训练数据准备
    * @return
    */
  def preTrainData(file:String) = {
    val data = sc.textFile(file).map(_.split("\t")).map { x => (x(0).split(",").toSeq,
      x(1).toLong + 1)}
    data
  }

  /**
    * 标准tfidf模型训练流程化
    */
  def gettfidfModel(file:String) = {
    val data = preTrainData(file)
    val model = trainModel(data)
    model
  }

  def main(args: Array[String]): Unit = {

    cosSimilarity("量化投资管理")

  }
}
