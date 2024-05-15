const SearchRecommender = require('../Schema/Recommender');

// Controller to insert or update a query in the collection
const insertOrUpdateQuery = async (req, res) => {
  const { query } = req.body;

  try {
    // Check if the query already exists in the collection
    const existingQuery = await SearchRecommender.findOne({ query });

    if (existingQuery) {
      // If query exists, update the rank by 1
      existingQuery.rank += 1;
      await existingQuery.save();
      res.status(200).json({ message: 'Query updated successfully' });
    } else {
      // If query does not exist, create a new entry with rank = 1
      const newQuery = new SearchRecommender({ query });
      await newQuery.save();
      res.status(201).json({ message: 'Query inserted successfully' });
    }
  } catch (error) {
    res.status(500).json({ error: 'Internal server error' });
  }
};

const getQueriesContainingInput = async (req, res) => {
    const input = req.params.word;
  
    try {
      // Find queries containing the input, sorted in descending order of rank and limited to 10 results
      const queries = await SearchRecommender.find({ query: { $regex: input, $options: 'i' } })
        .sort({ rank: -1 })
        .limit(10);
  
      res.status(200).json(queries);
    } catch (error) {
      res.status(500).json({ error: 'Internal server error' });
    }
  };
  
module.exports = { insertOrUpdateQuery , getQueriesContainingInput };
