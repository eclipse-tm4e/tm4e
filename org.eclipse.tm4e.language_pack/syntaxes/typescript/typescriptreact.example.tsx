import React, { useState, useEffect } from 'react';

// TypeScript Interface
interface Person {
  name: string;
  age: number;
}

// Functional Component with TypeScript Props
const ExampleComponent: React.FC<Person> = ({ name, age }) => {
  // State and Effect Hook with TypeScript
  const [count, setCount] = useState<number>(0);

  useEffect(() => {
    console.log('Component is mounted or count is updated');
    return () => {
      console.log('Component will unmount');
    };
  }, [count]);

  // JSX Elements with TypeScript
  return (
    <div>
      <h1>Hello, {name}!</h1>
      <p>Age: {age}</p>
      <p>Count: {count}</p>

      {/* Conditional Rendering */}
      {count > 5 && <p>Count is greater than 5.</p>}

      {/* Event Handling */}
      <button onClick={() => setCount(count + 1)}>Increment Count</button>

      {/* JSX Fragments */}
      <>
        <p>Fragment Example</p>
        <p>Another Fragment Element</p>
      </>

      {/* Array Mapping */}
      {[1, 2, 3].map((num) => (
        <span key={num}>{num}</span>
      ))}
    </div>
  );
};

// JSX Component Composition
const App: React.FC = () => {
  return (
    <>
      <ExampleComponent name="Alice" age={25} />
      <ExampleComponent name="Bob" age={30} />
    </>
  );
};

export default App;
