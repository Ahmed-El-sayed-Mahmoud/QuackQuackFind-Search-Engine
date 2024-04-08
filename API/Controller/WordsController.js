const Words = require("../Schema/WordsSchema")
const URLController = require("./UrlController")
const URLMiddelWare = require("../MiddelWare/UrlMiddleWare")
///////////////////////Insert////////////////////////////////////////
const FinalInsert = async (req, res) => {
    const { Word, Occure } = req.body
    if (Occure == null)
        res.status(400).json({ message: "Occure is reqy=uired" })
    const word = await Words.findOne({ Word: Word })
    if (word != null) {
        {
            await Update(word, req, res)
        }
        // await UpdateIDF(countDocuments)
    }

    else
        await InseartWordForFirstTime(req, res)




}
const InseartWordForFirstTime = async (req, res) => {
    const { Word, URL ,Occure} = req.body
    try {
        const  url= await (URLController.GetInfoByURL(URL))
        if(url!=null)
        {
        const{ _id, NumberofWords }=url
        const TF = CalculateTF(Occure, NumberofWords)
        const countDocuments = await URLController.GetNumbersofURL()
        const IDF = CalculateIDF(countDocuments, 1)

        const word = await Words.create({ Word: Word, Urls: [{ Url: _id, NumofOccure: Occure, TF }], IDF: IDF })
        res.status(201).json({ message: "Inserted Successfully" })}
        else
        res.status(400).json({message:"this link isnot exist"})
    }
    catch (e) {
        console.error(e)
    }



}
const Update = async (word, req, res) => {
    const { URL, Occure } = req.body
    try {
        const url = await (URLController.GetInfoByURL(URL))
         if (url == null)
             res.status(404).json({ message: "This Url dosent exist " })
         else {
        const { _id, NumberofWords } = url//get url inforamtion
        // console.log(_id)
        const instance = word//get word instance from database
        const urls = instance.Urls//get lists of links
        // const existURL = urls.findIndex((obj) => (obj.Url).toString() === (_id).toString())
        // if (existURL >= 0)//exist in this link before
        // {
        //     const existIndex = urls[existURL]
        //     const TF = CalculateTF(existIndex.NumofOccure + 1, NumberofWords)//Numberofoccure will increase by 1
        //     const countDocuments = await URLController.GetNumbersofURL()
        //     const IDF = CalculateIDF(countDocuments, urls.length)//the same
        //     instance.Urls[existURL] = { Url: _id, NumofOccure: existIndex.NumofOccure + 1, TF }
        //     instance.IDF = IDF
        //     await instance.save()
        // }

        // else //not exist before
        // {
        const TF = CalculateTF(Occure, NumberofWords)//number of occure will bw 1 it is first url time
        const countDocuments = await URLController.GetNumbersofURL()
        const IDF = CalculateIDF(countDocuments, urls.length + 1)//increase array by one number of links which have this word

        urls.push({ Url: _id, NumofOccure:Occure , TF })//push it
        instance.Urls = urls
        instance.IDF = IDF

        await instance.save()
        // await UpdateIDF(countDocuments)

        ///////////////update IDF for all words after increment number of total documents or number of document related to  word
        // }
        res.status(200).json({ message: "Inserted Successfully" })}
    }



    catch (e) {
        console.error(e)
    }






}

function CalculateTF(NumberofOccure, NumberofWords) {
    return (NumberofOccure / NumberofWords)
}
function CalculateIDF(NumberofTotalDocuments, NumberofDocuments) {
    return Math.log((NumberofTotalDocuments / NumberofDocuments))
}
//important
//every change of number of url or word's url so must call after insert a link with his all words  or after delete a link
const UpdateIDF = async (req, res) => {

    const allWord = await Words.find({})

    for (let obj of allWord) {
        obj.IDF = CalculateIDF(await (URLController.GetNumbersofURL()), (obj.Urls).length)
        await obj.save()
    }
    res.status(200).json({ message: "Updated all the database" })
}
////////////////////////////Delete//////////////////////////////////////////
const DeleteWordInaURL = async (req, res) => {
    if (req.body.URL == null)
        res.status(400).json({ message: "URL is required" })
    else {
        const Url = await URLController.GetInfoByURL(req.body.URL)
        if (Url == null)
            res.status(404).json({ message: "This link is not exist" })

        else {
            const word = req.Word
            let ListofURL = word.Urls
            const sizeBefore = ListofURL.length
            ListofURL = ListofURL.filter(url => (url.Url).toString() != Url._id)
            const sizeAfter = ListofURL.length
            if (sizeBefore != sizeAfter) {
                word.Urls = ListofURL
                await word.save()
                if (sizeAfter == 0) {
                    await Words.deleteOne(word)
                }

                res.status(204).json({ message: "Deleted" })
            }
            else {
                res.status(404).json({ message: "This word in not is this link" })
            }

        }
    }
}


const GetWordInfo = async (req, res) => {
    const word = req.params.word
    const urls = word.Urls
    let result = []
    for (const obj of urls) {
        const { TF, Url } = obj
        const { URL, Title, Rank } = await URLController.GetInfoByID(Url)
        const instance = { URL: URL, Title: Title, Rank: Rank, TF: parseFloat((TF).toString()) }
        result.push(instance)

    }
    res.status(200).json({ message: "Send Information Successfully", URLS: result, IDF: parseFloat((word.IDF).toString()) })
}


////////////////////////////////////////////////////////

module.exports = { FinalInsert, DeleteWordInaURL, UpdateIDF, GetWordInfo }






