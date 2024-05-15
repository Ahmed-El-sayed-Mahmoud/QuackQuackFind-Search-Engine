const mongoose = require('mongoose');

// Define the schema for the search recommender
const searchSchema = new mongoose.Schema({
  query: {
    type: String,
    required: true,
    unique: true, // Ensure queries are unique
  },
  rank: {
    type: Number,
    default: 1, // Initial rank value
  },
});


const SearchRecommender = mongoose.model('Recommender', searchSchema);

module.exports = SearchRecommender;