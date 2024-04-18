const Words = require("../Schema/WordsSchema")

//////////////////////////////////////////////////////
const ExistMiddleWare = async (req, res, next) => {
    const word = req.params.word;

    if (word == null)
        res.status(400).json({ message: "Word is required" });
    else {
        const Word = await Words.findOne({ Word: word });

        if (Word != null) {
            req.params.word = Word;//Return element
            next()
        }
        else {
            res.status(404).json({ message: "This Word is not  existing" });
        }
    }

}
/////////////////////////////////////////////////////
const CheckData = async (req, res, next) => {
    if (req.body.Word == null)
        res.status(400).json({ message: "Word is required" });
    else if (req.body.id == null)
        res.status(400).json({ message: "id is required" });
    else if (req.body.NumberofWords == null)
        res.status(400).json({ message: "NumberofWords is required" });
    else
        next()
}



module.exports = { ExistMiddleWare, CheckData }
