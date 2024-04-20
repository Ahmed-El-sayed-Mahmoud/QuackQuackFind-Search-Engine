const Words = require("../Schema/WordsSchema")
const URLController = require("./UrlController")
const URLMiddelWare = require("../MiddelWare/UrlMiddleWare")
///////////////////////Insert////////////////////////////////////////
const Update = async (req, res) => {
    const { URL, Title, NumberofWords, Occure, Word } = req.body;
    try {

        
        //const url = await URLController.GetInfoByURL(URL);
        //if (url == null)
        //  res.status(404).json({ message: "This URL does not exist" });

        const TF = CalculateTF(Occure, NumberofWords); // Calculate TF

        // Check if word exists
        word = await Words.findOne({ "Word": Word })
        if (!word) {
            // If word doesn't exist, create a new one
            word = await Words.create({ Word: Word, Urls: [{ Url: URL, Title: Title, NumofOccure: Occure, TF }] })
        }
        else {
            // Check if urls is null or empty
            if (!word.Urls || word.Urls.length === 0) {
                word.Urls = []; // Initialize urls as an empty array
            }

            // Push new URL information to the urls array
            word.Urls.push({ Url: URL, Title: Title, NumofOccure: Occure, TF });

            // Update word with the updated urls array
            await word.save();
        }
        res.status(200).json({ message: "Inserted Successfully" });
        console.log("inserted")

    } catch (e) {
        console.error(e);
        res.status(500).json({ message: "Internal Server Error" });
    }
};


function CalculateTF(NumberofOccure, NumberofWords) {
    return (NumberofOccure / NumberofWords)
}
// function CalculateIDF(NumberofTotalDocuments, NumberofDocuments) {
//     return Math.log((NumberofTotalDocuments / NumberofDocuments))
// }
// //important
// //every change of number of url or word's url so must call after insert a link with his all words  or after delete a link
// const UpdateIDF = async (req, res) => {

//     const allWord = await Words.find({})

//     for (let obj of allWord) {
//         obj.IDF = CalculateIDF(await (URLController.GetNumbersofURL()), (obj.Urls).length)
//         await obj.save()
//     }
//     res.status(200).json({ message: "Updated all the database" })
// }
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
    //let result = []
    // for (const obj of urls) {
    //     const { TF, Url } = obj
    //     const { URL, Title, Rank } = await URLController.GetInfoByID(Url)
    //     const instance = { URL: URL, Title: Title, Rank: Rank, TF: parseFloat((TF).toString()) }
    //     result.push(instance)

    // }
    res.status(200).json({ message: "Send Information Successfully", URLS: urls })
}



////////////////////////////////////////////////////////

module.exports = { Update, DeleteWordInaURL, GetWordInfo }






