import React from 'react';
import { connect } from 'react-redux'

import { updateAnnotation } from '../actions/updateAnnotation';
import { removeAnnotation } from '../actions/removeAnnotation';

const EditAnnotation = ({annotation, updateAnnotation, removeAnnotation}) => (
    <div>
        <textarea value={annotation.content} onChange={e => updateAnnotation(annotation.id, e.target.value)} />
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
