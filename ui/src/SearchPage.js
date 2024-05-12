import React, { useState } from 'react';
//import "./SearchPage.css";
import Doc from "./Doc";

const SearchPage = ({ onSearch }) => {
  const [searchResults, setSearchResults] = useState([]);
  const [input, setinput] = useState("");
  const [shouldUpdateResults, setShouldUpdateResults] = useState(false);
  const [CurrentPage,SetCurrentPage]=useState(0);
const [TenSearchResult,SetTenSearchResult]=useState([]);

  const handleSearch = async () => {
    if (input.trim() !== '') {
      try {
        const response = await fetch(`http://localhost:8090/search/${input}`);
        if (response.ok) {
          const data = await response.json();
          console.log(searchResults)
          setSearchResults(data)
          SetTenSearchResult(data.slice(0,10));
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
  function ChangePage(value) {
    SetCurrentPage(value);
    console.log(value)
    const searchData = {
      NormalQuery: input,
      docs: searchResults.slice(value*10,value*10+10)
    }
    console.log("data sent :",JSON.stringify(searchData) )
    fetch('http://localhost:8090/api', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(searchData),
    })
    
    .then(response => response.json())
    .then(data => {
      console.log('Success:', data);
      SetTenSearchResult(data)
      setShouldUpdateResults(true);
    })
    .catch(error => {
      console.error('Error:', error);
      // Handle errors here
    });
    
  }

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
        {shouldUpdateResults && TenSearchResult.map((doc, index) => (
          <Doc key={index} doc={doc} />
        ))}
      </div>
      <button onClick={() => ChangePage(1)}>2</button>
      <button onClick={() => ChangePage(2)}>3</button>
      <button onClick={() => ChangePage(3)}>4</button>
      <button onClick={() => ChangePage(4)}>5</button>
      <button onClick={() => ChangePage(5)}>6</button>
      <button onClick={() => ChangePage(6)}>7</button>
    </>
  );
};

export default SearchPage;
