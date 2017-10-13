import { connect } from 'react-redux'

import { fetchAnnotationHistory } from '../../actions/fetchAnnotationHistory'

import History from './history'

export default connect(
    (state) => ({
        annotationHistory: state.history
    }),
    {
        fetchAnnotationHistory
    }
)(History)
