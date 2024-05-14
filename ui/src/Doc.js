import React from 'react';

const Doc = ({ doc }) => {
  return (
    <div className="doc-container">
      <a href={doc["Url"]} target="_blank" rel="noopener noreferrer"><h3>{doc["Title"]}</h3></a>
      {doc["Url"]}
      <div className='lolo'><span dangerouslySetInnerHTML={{ __html: doc["Describtion"] }}></span>
      </div>
      
    </div>
  );
};

export default Doc;
