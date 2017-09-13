import React from 'react';
import { connect } from 'react-redux'

import { updateAnnotation, removeAnnotation } from '../actions';

const EditAnnotation = ({annotation, updateAnnotation, removeAnnotation}) => (
    <div>
        <textarea value={annotation.content} onChange={e => updateAnnotation(e.target.value)} />
        <button onClick={() => removeAnnotation(annotation.id)}>Remove Annotation</button>
    </div>
)


export default connect(
  (state) => ({
     annotation: state.annotation
  }),
  {
     updateAnnotation,
     removeAnnotation
  }
)(EditAnnotation)
