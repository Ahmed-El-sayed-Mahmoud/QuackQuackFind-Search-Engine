import React, { useState } from 'react';
//import "./SearchPage.css";
import Doc from "./Doc";

const SearchPage = ({ onSearch }) => {
  const [searchResults, setSearchResults] = useState([]);
  const [input, setinput] = useState("");
  const [shouldUpdateResults, setShouldUpdateResults] = useState(false);

  const handleSearch = async () => {
    if (input.trim() !== '') {
      try {
        const response = await fetch(`http://localhost:8090/search/${input}`);
        if (response.ok) {
          const data = await response.json();
          console.log(searchResults)
          setSearchResults(data);
          setShouldUpdateResults(true); // Trigger update of specific part
        } else {
          console.error('Error fetching search results:', response.statusText);
        }
      } catch (error) {
        console.error('Error fetching search results:', error);
      }
    }
  };

  const handleKeyPress = (event) => {
    if (event.key === 'Enter') {
      handleSearch();
    }
  };

  return (
    <>
      <header className="search_header">
        <div className="header-container">
          <h1 className="header-title">Quack Quack Find</h1>
          <div className="search-cont">
            <input
              type="text"
              placeholder="Search..."
              value={input}
              onChange={(e) => setinput(e.target.value)}
              //onKeyDown={handleKeyPress}
            />
            <button onClick={handleSearch}>Search</button>
          </div>
        </div>
      </header>
      <div className="search-results">
        {/* Only render Doc components if shouldUpdateResults is true */}
        {shouldUpdateResults && searchResults.map((doc, index) => (
          <Doc key={index} doc={doc} />
        ))}
      </div>
    </>
  );
};

export default SearchPage;
