const Url = require("../Schema/UrlSchema")
// const WordMiddelWare = require("../MiddelWare/WordsMiddeleWare")

///////////////////////Insert////////////////////////////////////////
const InseartURL = async (req, res) => {
    const { URL, Title, Rank, NumberofWords } = req.body
    const exist = await Url.findOne({ URL: URL });
    if (exist != null)
        res.status(409).json({ message: "Exist" })
    else{
    const url = await Url.create({ URL: URL, Title: Title, Rank: Rank, NumberofWords: NumberofWords });
    res.status(201).json({ message: "Created Successfully", id: url._id })
    // console.log(url);
    }


}

////////////////////////////Delete//////////////////////////////////////////
const DeleteURL = async (req, res) => {
    await Url.deleteOne(req.URL)
    // const counts = await Url.countDocuments()//after delete a url this will decrease the number of urls so must update IDF
    // await WordMiddelWare.UpdateIDF(counts)
    res.status(204).json({ message: "Deleted" })
}
///////////////////////////Update///////////////////////////////////////////
const UpdateURL = async (req, res) => {

    (req.URL).NumberofWords = req.body.NumberofWords
    await (req.URL).save()
    res.status(200).json({ message: "Updated successfully" })
}

//////////////////////////Get/////////////////////if needed not necessary
const GetInfo = async (req, res) => {
    const BodyUrl = req.URL
    res.status(200).json(BodyUrl)

}

///////////////////////to call in words schema///////////////////////////
const GetInfoByID = async (id) => {
    try {
        const url = await Url.findById(id)
        return url
    }
    catch (e) {
        console.error(e)

    }
}
const GetInfoByURL = async (URL) => {
    try {
        const url = await Url.findOne({ URL: URL })
        return url
    }
    catch (e) {
        console.error(e)
    }
}
//Number of urls in schema
const GetNumbersofURL = async (req, res) => {
    return await Url.countDocuments()
}

module.exports = { InseartURL, DeleteURL, UpdateURL, GetInfo, GetInfoByID, GetInfoByURL, GetNumbersofURL }







