const Url = require("../Schema/UrlSchema")
const InseartMiddelWare = async (req, res, next) => {

    const bodyURL = req.body.URL;
    const exist = await Url.exists({ URL: bodyURL });
    if (!exist) {
        next()
    }
    else
        res.status(409).json({ message: "This Url is already existing" });
}
//////////////////////////////////////////////////////
const ExistMiddleWare = async (req, res, next) => {
    const bodyURL = req.body.URL;
    if (bodyURL == null||bodyURL=="")
        res.status(400).json({ message: "URL is required" });
    else {
        const url = await Url.findOne({ URL: bodyURL });
        if (url != null) {
            req.URL = url;//Return element
            next()
        }
        else {
            res.status(404).json({ message: "This URL is not  existing" });
        }
    }

}

/////////////////////////////////////////////////////
const CheckData = async (req, res, next) => {
    if (req.body.URL == null||req.body.URL=="")
        res.status(400).json({ message: "URL is required" });
    else if (req.body.Title == null||req.body.Title=="")
        res.status(400).json({ message: "Title is required" });
    else if (req.body.Rank == null)
        res.status(400).json({ message: "Rank is required" });
    else if (req.body.NumberofWords == null)
        res.status(400).json({ message: "NumberofWords is required" });

    else
        next()




}
const UpdateMiddelWare = async (req, res, next) => {
    if (req.body.NumberofWords == null)
        res.status(400).json({ message: "NumberofWords is required" });
    else
        next()
}
///////////////////////to use it in Words Schema
const IsExistForWordsSchema = async (id) => {
    const exist = await Url.exists({ _id: id })
    if (!exist)
        return false
    else return true
}

module.exports = { InseartMiddelWare, ExistMiddleWare, CheckData, UpdateMiddelWare, IsExistForWordsSchema }
