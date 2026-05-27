import { useParams } from "react-router-dom";

export const About = () => {
  const params = useParams();
  console.log(params);
  return (
    <div style={{ padding: 20 }}>
      <h2>About Page</h2>
      <p>This is a React 18 + Rsbuild project.</p>
    </div>
  );
};
