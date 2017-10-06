import { connect } from 'react-redux'

import {
    updateAnnotation,
    editAnnotation
} from '../../actions/updateAnnotation'

import { removeAnnotation } from '../../actions/removeAnnotation'

import EditAnnotation from './editAnnotation'

export default connect(
  (state) => ({
     annotation: state.singleAnnotation.annotation
  }),
  {
     editAnnotation,
     updateAnnotation,
     removeAnnotation
  }
)(EditAnnotation)
